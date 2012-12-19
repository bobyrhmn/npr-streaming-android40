/*
THIS SOFTWARE IS PROVIDED BY ANDREW TRICE "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL ANDREW TRICE OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.streaming;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

/**
 * @author Poornima
 *
 *http://stackoverflow.com/questions/8681550/android-2-2-mediaplayer-is-working-fine-with-one-shoutcast-url-but-not-with-the
 *
 */
public class StreamAudio extends Plugin {
	
	public static final int numberOfTempFiles = 10;
	//public static String dirPath = "/sdcard0/data/NPRmedia/";
	public static String dirPath;
	
	private int INTIAL_KB_BUFFER ;
    private int BIT = 8 ;
    private int SECONDS = 10 ;
    private File downloadingMediaFile ; 
	private String FILE_NAME_SDCARD = "media";
	private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
	private MediaPlayer mediaPlayer = null;
	private String TAG = "NPRPlayMusic";
	  
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	 
	private boolean isInterrupted; 
	private boolean firstTime = true;
	private int counter = 0;

	public static String streamURL;
	public static final String ERROR_NO_AUDIOID="A reference does not exist for the specified audio id.";
 	public static final String BUFFER_AUDIO="bufferRadio";
	public static final String PLAY_AUDIO="playRadio";
	public static final String STOP_AUDIO="stopRadio";
 	public PluginResult result = null;
	public PlayMusic playMusic = null;
	
	private Thread downloadStreamThread;

	@SuppressWarnings("deprecation")
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) 
	{	
		
		//Get the external storage environment.
		dirPath = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS).toString() + "/NPRmedia/";
		 
		playMusic = new PlayMusic(dirPath+"play_"+ FILE_NAME_SDCARD);
		checkDirContents();

		try {
			streamURL = data.getString(0);
			Log.d(streamURL, action);
			if (BUFFER_AUDIO.equals( action ) ) {
				this.startBuffering(streamURL, 48);
			}
			else if (STOP_AUDIO.equals(action )){
				setInterruptThread();
				//stop the music
				playMusic.stopMusic();
				//send interrupt to playing thread to cleanup
				playMusic.interrupt();
				playMusic.stop();
				//wait for download thread to complete and then do cleanup of media files
				downloadStreamThread.join(5000);
				checkDirContents();
				result = new PluginResult(Status.OK, "Yay, Success!!!");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			result = new PluginResult(Status.ERROR, "Some, error!!!");
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return result;
	}


	/**
	 * Synchronized method invoked to set the interrupt to true
	 */
	private synchronized void setInterruptThread(){
		Log.d(TAG,"Setting interrupt to true");
		isInterrupted = true;
	}
	
	/**
	 * Synchronized method invoked to set the interrupt to false.
	 */
	private synchronized void ResetInterruptThread(){
		Log.d(TAG,"Resetting interrupt to true");
		isInterrupted = false;
	}

	/**
	 * This method invokes a thread to begin buffering and it keeps preparing the 
	 * temp buffer as multiple files and the media player can pick those files to play music from them.
	 * @param mediaUrl The url of radio to play from.
	 * @param bitrate Bitrate of HTTP Streaming
	 * @throws IOException
	 */
	public void startBuffering(final String mediaUrl, int bitrate) throws IOException{
    	final String TAG = "startStreaming";
    	//Set up buffer size
    	//Assume XX kbps * XX seconds / 8 bits per byte
    	INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT;
    	downloadStreamThread = new Thread() {
			public void run() {
				try {
					downloadingMediaFile = new File(dirPath, FILE_NAME_SDCARD + counter);
					downloadAudioIncrement(mediaUrl);
				} catch (IOException e) {
					Log.d(TAG, "Initialization error for url=" + mediaUrl, e);
					return;
				}
			}
		};
		downloadStreamThread.start();		 
	}	
	
	/**
	 * This method downloads the bytes from the url and buffer the byte and pass the temp file(in form of bytes) to the media player engine.
	 * @param mediaUrl The url to download bytes from
	 * @throws IOException Incase of some exception in the buffering
	 */
	public void downloadAudioIncrement(String mediaUrl) throws IOException {
	   	URLConnection cn = new URL(mediaUrl).openConnection(); 
        cn.connect();   
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(TAG, "Unable to create InputStream for mediaUrl: " + mediaUrl);
        }
        
		Log.i(TAG, "File name: " + downloadingMediaFile);
		BufferedOutputStream bout = new BufferedOutputStream ( new FileOutputStream(dirPath + FILE_NAME_SDCARD + counter), 32 * 1024);   
        byte buf[] = new byte[16 * 1024];
        int totalBytesRead = 0;
        while (!isInterrupted) {
        	if (bout == null) {
        		if(counter >= numberOfTempFiles) //to have just 5 files for the download stream
        			counter=0;
        		else
        			counter++;
        		
        		Log.d(TAG, "Creating file : " + FILE_NAME_SDCARD + counter);
        		downloadingMediaFile = new File(dirPath, FILE_NAME_SDCARD + counter);
        		bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile), 32 * 1024 );	
        	}
        	
        	//Read download stream and copies it into the buffer(buf)
        	int numread = stream.read(buf);  
            if (numread <= 0) {  
                break;   
            } else {
                bout.write(buf, 0, numread); //write the current read stream into the temp file
                totalBytesRead += numread;
                totalKbRead = totalBytesRead/1000;
            }         
            
            if ( totalKbRead >= INTIAL_KB_BUFFER && !isInterrupted) {
            	//Log.v(TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
            	bout.flush();
            	bout.close();
            	bout = null;
            	File temp = new File(dirPath+"play_"+ FILE_NAME_SDCARD + counter+ ".dat");
            	downloadingMediaFile.renameTo(temp);
            	Log.d(TAG,"Renamed file to " + downloadingMediaFile.toString() );
            	totalBytesRead = 0;
          		
            	if(firstTime){
            		firstTime = false;
            		//Thread musicPlayingThread = new Thread(playMusic);
            		//Log.d(TAG,"Invoking musicPlayingThread second thread");
            		playMusic.start();
            	}
            }           
        }  
       	stream.close();
       	Log.d(TAG,"Download thread resetting counter and exiting ");
       	//reset counter to 0
       	ResetInterruptThread();
       	firstTime =true;
       	counter=0;
	}
 
 	//To create new temp directory and also to delete the temp files upon app exit.
 	public static void checkDirContents() {
 	    File file = new File(dirPath);
 	    if (file.exists()) {
 	    	 File[] files = file.listFiles();
 	        if(files!=null) { //some JVMs return null for empty dirs
 	        	for (File f: files) f.delete();
 	        }
 	    }else{
 	    	new File(dirPath).mkdir();
 	    }
 	}
} 