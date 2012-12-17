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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

/**
 * @author Poornima
 *
 *http://stackoverflow.com/questions/8681550/android-2-2-mediaplayer-is-working-fine-with-one-shoutcast-url-but-not-with-the
 *
 */
public class StreamAudio extends Plugin {
	
	public static final int numberOfTempFiles = 10;
	public static String dirPath = "/sdcard/data/NPRmedia/";
	private int INTIAL_KB_BUFFER ;
    private int BIT = 8 ;
    private int SECONDS = 10 ;
    private File downloadingMediaFile ; 
	private String FILE_NAME_SDCARD = "media";
	private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
	private MediaPlayer mediaPlayer = null;
	private String TAG = "PoorniPlayMusic";
	  
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

	
	private synchronized void setInterruptThread(){
		Log.d(TAG,"Setting interrupt to true");
		isInterrupted = true;
	}
	private synchronized void ResetInterruptThread(){
		Log.d(TAG,"Resetting interrupt to true");
		isInterrupted = false;
	}
/*
	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "Stream is prepared");

	}

	private void play() {
		Uri myUri = Uri.parse(streamURL);
		try {
			if (mediaPlayer == null) {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.reset();

			} else {
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
			try{
				//mp.setDataSource(ctx.getContext(), myUri); // Go to Initialized state
				mediaPlayer.setDataSource(myUri.toString()); // Go to Initialized state
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.prepare();
				mediaPlayer.start();
			}
			catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mediaPlayer.reset();
				mediaPlayer.prepare();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mediaPlayer.start();
			if(mediaPlayer.isPlaying()){
				mediaPlayer.setOnBufferingUpdateListener(this);
			}
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnErrorListener(this);



			Log.d(TAG, "LoadClip Done");
		} catch (Throwable t) {
			Log.d(TAG, t.toString());
		}
	}

	private void pause() {
		mediaPlayer.pause();
	}

	private void stop() {
		mediaPlayer.stop();
		isInterrupted = true;
	}
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
	}

	public void onCompletion(MediaPlayer mp) {
		stop();
	}
	public boolean onError(MediaPlayer mp, int what, int extra) {
		StringBuilder sb = new StringBuilder();
		sb.append("Media Player Error: ");
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			sb.append("Not Valid for Progressive Playback");
			break;
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			sb.append("Server Died");
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			sb.append("Unknown");
			break;
		default:
			sb.append(" Non standard (");
			sb.append(what);
			sb.append(")");
		}
		sb.append(" (" + what + ") ");
		sb.append(extra);
		Log.e(TAG, sb.toString());
		return true;
	}


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