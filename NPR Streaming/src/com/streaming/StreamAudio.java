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

import java.io.IOException;

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
	private MediaPlayer mp = null;
	private String TAG = getClass().getSimpleName();
	
	public static String streamURL;
	public static final String ERROR_NO_AUDIOID="A reference does not exist for the specified audio id.";
	public static final String PRELOAD_AUDIO="preloadAudio";
	public static final String BUFFER_AUDIO="bufferAudio";
	public static final String PLAY_AUDIO="playAudio";
	public static final String PLAY="play";
	public static final String STOP="stop";
	public static final String LOOP="loop";
	public static final String UNLOAD="unload";


	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "Stream is prepared");
	
	}

	private void play() {
		
		Uri myUri = Uri.parse(streamURL);
		try {
			if (mp == null) {
				mp = new MediaPlayer();
				mp.reset();
				 
			} else {
				mp.stop();
				mp.reset();
			}
			try{
				mp.setDataSource(ctx.getContext(), myUri); // Go to Initialized state
				mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mp.prepare();
			}
			catch(IllegalStateException e){
				mp.reset();
				mp.prepare();
			}
			
			
			
			mp.start();
			/*if(mp.isPlaying()){
				mp.setOnBufferingUpdateListener(this);
			}
			mp.setOnPreparedListener(this);
			mp.setOnErrorListener(this);
			*/
			

			Log.d(TAG, "LoadClip Done");
		} catch (Throwable t) {
			Log.d(TAG, t.toString());
		}
	}

	private void pause() {
		mp.pause();
	}

	private void stop() {
		mp.stop();

	}


	@SuppressWarnings("deprecation")
	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) 
	{
		PluginResult result = null;
		 
		try {
			streamURL = data.getString(0);
			Log.d(streamURL, action);
			 

			if (PRELOAD_AUDIO.equals( action ) ) {
				//mp.setDataSource(streamURL);
				result = new PluginResult(Status.OK, "Yay, Success!!!");
			}else if (BUFFER_AUDIO.equals( action ) ) {
				//mp.setDataSource(streamURL);
				result = new PluginResult(Status.OK, "Yay, Success!!!");
			}else if (PLAY_AUDIO.equals(action )){
				this.play();
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
		}  




		return result;
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





}