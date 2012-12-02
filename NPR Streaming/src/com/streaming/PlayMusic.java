package com.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class PlayMusic implements Runnable, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnBufferingUpdateListener
{
	public static final int numberOfTempFiles = 2;
	private String tempFileLocation;
	private MediaPlayer mediaPlayer = null;
	private boolean isInterrupted = false;
	private String TAG = "PoorniPlayMusic";
	private String COMPLETE_TAG = "Poorni.OnCompletionListener";

	private int counter = 0;
	/**
	 * Test whether we need to transfer buffered data to the MediaPlayer.
	 * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
	 */  

	public PlayMusic(String _filePath){
		tempFileLocation = _filePath;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "Stream is prepared");

	}

	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.d(TAG, "PlayerService onBufferingUpdate : " + percent + "%");
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

	public void stopMusic(){
		isInterrupted = true;
		mediaPlayer.stop();
		
		
	}
	
	private MediaPlayer updateFileLocation(MediaPlayer mPlayer)
			throws IOException {
		Log.i(TAG, "Setting  read file= " + tempFileLocation+ counter + ".dat");
		File bufferedFile = new File(tempFileLocation+ counter + ".dat");

		
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
		FileInputStream fis = new FileInputStream(bufferedFile);
		mPlayer.setDataSource(fis.getFD());
		//Log.i(TAG, "Datasource has been changed. counter=" + counter);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		//TODO PREPARE ASYNC 
		mPlayer.prepare();
		return mPlayer;
	}

	@Override
	public void run() {
		Log.i(TAG, "The thread has started");
 		mediaPlayer = new MediaPlayer();
 			try {
				MediaPlayer mp = this.updateFileLocation(mediaPlayer);
				mp.setOnCompletionListener(this);
				mp.start();
				Log.i(TAG, "MediaPlayer thread has started");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
 
	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "MediaPlayer thread is completed . counter= " + counter);
		if(counter >= numberOfTempFiles)
			counter = 0;
		else 
			counter++;
		 
		try {
			mp.reset();
			mp = this.updateFileLocation(mp);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		mp.start();
		Log.i(TAG, "MediaPlayer thread staring again.counter= " + counter);
		
	}
}
