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
public class StreamAudio extends Plugin implements
MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
MediaPlayer.OnErrorListener,      MediaPlayer.OnBufferingUpdateListener {
	
	private int INTIAL_KB_BUFFER ;
    private int BIT = 8 ;
    private int SECONDS = 30 ;
    private File downloadingMediaFile ; 
	private String DOWNFILE = "downloadingMediaFile";
	private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
	private int playedcounter = 0;
	
	private MediaPlayer mediaPlayer = null;
	private String TAG = "Poorni" + getClass().getSimpleName();
	  
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	 
	private boolean isInterrupted; 
	private int counter = 0;

	public static String streamURL;
	public static final String ERROR_NO_AUDIOID="A reference does not exist for the specified audio id.";
	public static final String PRELOAD_AUDIO="preloadAudio";
	public static final String BUFFER_AUDIO="bufferRadio";
	public static final String PLAY_AUDIO="playRadio";
	public static final String STOP_AUDIO="stopRadio";
	public static final String PLAY="play";
	public static final String STOP="stop";
	public static final String LOOP="loop";
	public static final String UNLOAD="unload";
	public PluginResult result = null;

	@SuppressWarnings("deprecation")
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) 
	{
		

		try {
			streamURL = data.getString(0);
			Log.d(streamURL, action);
			if (PRELOAD_AUDIO.equals( action ) ) {
				result = new PluginResult(Status.OK, "Yay, Success!!!");
			}else if (BUFFER_AUDIO.equals( action ) ) {
				this.startBuffering(streamURL, 48);
			}else if (PLAY_AUDIO.equals(action )){
				this.play();
				result = new PluginResult(Status.OK, "Yay, Success!!!");
			}else if (STOP_AUDIO.equals(action )){
				this.stop();
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
		}  
		return result;
	}



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



	public void startBuffering(final String mediaUrl, int bitrate) throws IOException{
    	final String TAG = "startStreaming";
    	//Set up buffer size
    	//Assume XX kbps * XX seconds / 8 bits per byte
    	INTIAL_KB_BUFFER =  bitrate * SECONDS / BIT;
    	
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					downloadingMediaFile = new File("/sdcard/data/", DOWNFILE + counter);
					downloadAudioIncrement(mediaUrl);
				} catch (IOException e) {
					Log.e(getClass().getName(), "Initialization error for url=" + mediaUrl, e);
					return;
				}
			}
		};
		new Thread(r).start();
	}

	public void downloadAudioIncrement(String mediaUrl) throws IOException {
		final String TAG = "downloadAudioIncrement";

    	URLConnection cn = new URL(mediaUrl).openConnection(); 
        cn.connect();   
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(TAG, "Unable to create InputStream for mediaUrl: " + mediaUrl);
        }
        
		Log.i(TAG, "File name: " + downloadingMediaFile);
		BufferedOutputStream bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile), 32 * 1024 );   
        byte buf[] = new byte[16 * 1024];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        boolean stop = false;
        do {
        	if (bout == null) {
        		counter++;
        		Log.i(TAG, "FileOutputStream is null, Create new one: " + DOWNFILE + counter);
        		downloadingMediaFile = new File("/sdcard/data/", DOWNFILE + counter);
        		bout = new BufferedOutputStream ( new FileOutputStream(downloadingMediaFile) );	
        	}

        	int numread = stream.read(buf);  
        	
            if (numread <= 0) {  
                break;   
            	
            } else {
            	//Log.v(TAG, "write to file");
                bout.write(buf, 0, numread);

                totalBytesRead += numread;
                incrementalBytesRead += numread;
                totalKbRead = totalBytesRead/1000;
            }
            
            
            
            if ( totalKbRead >= INTIAL_KB_BUFFER && ! stop) {
            	Log.v(TAG, "Reached Buffer amount we want: " + "totalKbRead: " + totalKbRead + " INTIAL_KB_BUFFER: " + INTIAL_KB_BUFFER);
            	bout.flush();
            	bout.close();
            	            	
            	bout = null;
            	
            	setupplayer(downloadingMediaFile);
            	totalBytesRead = 0;

            }
            
        } while (true);   

       	stream.close();

	}
   
	 
    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
    private void  setupplayer(File partofaudio) {
    	final File f = partofaudio;
    	final String TAG = "poorni setupplayer";
    	Log.i(TAG, "File " + f.getAbsolutePath());
	    Runnable r = new Runnable() {
	        public void run() {
	        	
	        	MediaPlayer mp = new MediaPlayer();
	        	try {
	        		
	        		MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener () {
	        			public void onCompletion(MediaPlayer mp){
	        				String TAG = "MediaPlayer.OnCompletionListener";
	        				 
	        				Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
	        				while (mediaplayers.size() <= 1){
    			        		Log.v(TAG, "waiting for another mediaplayer");
    			        	}
	        				MediaPlayer mp2 = mediaplayers.get(1);
    			        	mp2.start();
    			        	Log.i(TAG, "Start new player");
    			        	
	        				mp.release();
	        				mediaplayers.remove(mp);
	        				removefile();
	        				
	        			}
	        		};
	        		
	        		FileInputStream ins = new FileInputStream( f );
	            	mp.setDataSource(ins.getFD());
	        		mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        		
	        		mp.setOnCompletionListener(listener);
	        		Log.i(TAG, "Prepare Media Player " + f);
	        		
	        		if ( ! isInterrupted  ){
	        			mp.prepare();
	        		} else {
	        			//This will save us a few more seconds
	        			mp.prepareAsync();
	        		}
	        		
	        		mediaplayers.add(mp);
	        		if ( ! isInterrupted  ){
		        		Log.i(TAG, "Start Media Player " + f);
		        		startMediaPlayer();
		        	}
	        	} catch  (IllegalStateException	e) {
	        		Log.e(TAG, e.toString());
	        	} catch  (IOException	e) {
	        		Log.e(TAG, e.toString());
	        	}
	        	
 	        }
	    };
	    new Thread(r).start();

    }
    
    //Removed file from cache
    private void removefile (){
    	String TAG = "removefile";
    	File temp = new File("/sdcard/data/",DOWNFILE + playedcounter);
    	Log.i(TAG, temp.getAbsolutePath());
    	temp.delete();
    	playedcounter++;
    }
    
	private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				//mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
    }

    private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() { 
			public void run() {
   	        	transferBufferToMediaPlayer();

   	        	// Delete the downloaded File as it's now been transferred to the currently playing buffer file.
   	        	downloadingMediaFile.delete();
	        	StringBuffer buf = new StringBuffer("Audio full loaded: " + totalKbRead + " Kb read");
	        	result = new PluginResult(Status.OK, buf.toString());
	        	
	        }
	    };
	  //poorni handler.post(updater);
    }
    
	/**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
	private void  testMediaBuffer() {
	    Runnable updater = new Runnable() {
	        public void run() {
	            if (mediaPlayer == null) {
	            	//  Only create the MediaPlayer once we have the minimum buffered data
	            	if ( totalKbRead >= INTIAL_KB_BUFFER) {
	            		try {
		            		startMediaPlayer();
	            		} catch (Exception e) {
	            			Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
	            		}
	            	}
	            } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
	            	//  NOTE:  The media player has stopped at the end so transfer any existing buffered data
	            	//  We test for < 1second of data because the media player can stop when there is still
	            	//  a few milliseconds of data left to play
	            	transferBufferToMediaPlayer();
	            }
	        }
	    };
	    //poorni handler.post(updater);
    }
	
	private void startMediaPlayer() {
        try {   
        	File bufferedFile = new File("/sdcard/data/","playingMedia" + (counter++) + ".dat");
        	
        	// We double buffer the data to avoid potential read/write errors that could happen if the 
        	// download thread attempted to write at the same time the MediaPlayer was trying to read.
        	// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while 
        	// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack, 
        	// we move the currently loaded data to a temporary buffer file that we start playing while the remaining 
        	// data downloads.  
        	moveFile(downloadingMediaFile,bufferedFile);
    		
        	Log.e(getClass().getName(),"Buffered File path: " + bufferedFile.getAbsolutePath());
        	Log.e(getClass().getName(),"Buffered File length: " + bufferedFile.length()+"");
        	
        	mediaPlayer = createMediaPlayer(bufferedFile);
        	
    		// We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.
	    	mediaPlayer.start();
	    	//poorni startPlayProgressUpdater();        	
			//poorni playButton.setEnabled(true);
        } catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
        	return;
        }   
    }
	
	
	 private MediaPlayer createMediaPlayer(File mediaFile)
			    throws IOException {
			    	MediaPlayer mPlayer = new MediaPlayer();
			    	mPlayer.setOnErrorListener(
							new MediaPlayer.OnErrorListener() {
						        public boolean onError(MediaPlayer mp, int what, int extra) {
						        	Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
						    		return false;
						        }
						    });

					//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
					//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
					//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
					FileInputStream fis = new FileInputStream(mediaFile);
					mPlayer.setDataSource(fis.getFD());
					mPlayer.prepare();
					return mPlayer;
			    }
	 
    /**
     * Transfer buffered data to the MediaPlayer.
     * NOTE: Interacting with a MediaPlayer on a non-main UI thread can cause thread-lock and crashes so 
     * this method should always be called using a Handler.
     */  
     private void transferBufferToMediaPlayer() {
	    try {
	    	// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
	    	boolean wasPlaying = mediaPlayer.isPlaying();
	    	int curPosition = mediaPlayer.getCurrentPosition();
	    	//poorni
	    	// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later. 
	    	File oldBufferedFile = new File("/sdcard/data/","playingMedia" + counter + ".dat");
	    	File bufferedFile = new File("/sdcard/data/","playingMedia" + (counter++) + ".dat");

	    	//  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to 
	    	// keep and track fully downloaded files for later use, write caching code and please send me a copy.
	    	bufferedFile.deleteOnExit();   
	    	moveFile(downloadingMediaFile,bufferedFile);

	    	// Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),
	    	// this always happens so quickly that the user never realized we've stopped the player and started a new one
	    	mediaPlayer.pause();

	    	// Create a new MediaPlayer rather than try to re-prepare the prior one.
        	mediaPlayer = createMediaPlayer(bufferedFile);
    		mediaPlayer.seekTo(curPosition);
    		
    		//  Restart if at end of prior buffered content or mediaPlayer was previously playing.  
    		//	NOTE:  We test for < 1second of data because the media player can stop when there is still
        	//  a few milliseconds of data left to play
    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}

	    	// Lastly delete the previously playing buffered File as it's no longer needed.
	    	oldBufferedFile.delete();
	    	
	    }catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
     /**
      *  Move the file in oldLocation to newLocation.
      */
 	public void moveFile(File	oldLocation, File	newLocation)
 	throws IOException {

 		if ( oldLocation.exists( )) {
 			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );
 			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
             try {
 		        byte[]  buff = new byte[8192];
 		        int numChars;
 		        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
 		        	writer.write( buff, 0, numChars );
       		    }
             } catch( IOException ex ) {
 				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
             } finally {
                 try {
                     if ( reader != null ){                    	
                     	writer.close();
                         reader.close();
                     }
                 } catch( IOException ex ){
 				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
 				}
             }
         } else {
 			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
         }
 	}
 	
    private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
	        public void run() {
	        	//textStreamed.setText((totalKbRead + " Kb read"));
	    		float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
	    		//progressBar.setSecondaryProgress((int)(loadProgress*100));
	        }
	    };
	  //poorni handler.post(updater);
    }
    
     
} 