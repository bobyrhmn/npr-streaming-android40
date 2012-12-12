package com.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class PlayMusic extends Thread implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnBufferingUpdateListener
{
	public static final int numberOfTempFiles = 10;
	public static final int maxRetryAttempt = 5;
	private boolean isInterrupted;
	public boolean isInterrupted() {
		return isInterrupted;
	}

	public void setInterrupted(boolean isInterrupted) {
		this.isInterrupted = isInterrupted;
	}
	private String tempFileLocation;
	private MediaPlayer mediaPlayer = null;
	//private boolean isInterrupted = false;
	private String TAG = "PoorniPlayMusic";


	private int counter = 0;
	/**
	 * Test whether we need to transfer buffered data to the MediaPlayer.
	 * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
	 */  
	public PlayMusic(String _filePath){
		tempFileLocation = _filePath;
		mediaPlayer = new MediaPlayer();
		setInterrupted(false);
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
		Log.d(TAG,"playmusic thread stopping music");
		setInterrupted(true);
		mediaPlayer.stop();

	}

	private MediaPlayer updateFileLocation(MediaPlayer mPlayer)
			throws IOException {
		Log.d(TAG, "Setting  read file= " + tempFileLocation+ counter + ".dat");



		mPlayer.setOnErrorListener(
				new MediaPlayer.OnErrorListener() {
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Log.d(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );
						return false;
					}
				});

		//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
		//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
		//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
		File bufferedFile = new File(tempFileLocation+ counter + ".dat");
		int i = 0;
		while( (!bufferedFile.exists() && i < maxRetryAttempt) || getInterrupted()){
			try {
				Log.d(TAG, tempFileLocation+ counter + ".dat is not yet created.So sleeping");
				i++;
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(bufferedFile.exists()){
			FileInputStream fis = new FileInputStream(bufferedFile);
			mPlayer.setDataSource(fis.getFD());
			//Log.i(TAG, "Datasource has been changed. counter=" + counter);
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

			//TODO PREPARE ASYNC 
			mPlayer.prepare();
			return mPlayer;
		}else{
			throw new IOException();
		}
	}

	@Override
	public void run() {
		Log.d(TAG, "In PlayMusic file, starting media player thread.");
		MediaPlayer mp =null;
		try {
			 mp = this.updateFileLocation(mediaPlayer);
			sleep(0);		
			mp.setOnCompletionListener(this);
			mp.start();
			Log.d(TAG, "MediaPlayer is invoked");
		}
		catch (InterruptedException ex) {
			cleanup(mp);
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	private void cleanup(MediaPlayer mp)
	{
		Log.d(TAG,"Playmusic thread releasing media plyr res and exiting");
		mp.release();
		counter=0;
		return;
	}
	public void onCompletion(MediaPlayer mp) {
		
		if (Thread.interrupted()) {
			cleanup(mp);
			return;
		}
		if(getInterrupted())
		{
			Log.d(TAG, "interrupt is true and hence music thread exit");
			return;
		}
			
		Log.d(TAG, "MediaPlayer counter= " + counter + "is finished.");
		if(counter >= numberOfTempFiles)
			counter = 0;
		else 
			counter++;

		try {
			sleep(0);
			mp.reset();
			mp = this.updateFileLocation(mp);
		}
		catch (InterruptedException ex) {
			cleanup(mp);
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
		mp.start();
		Log.d(TAG, "MediaPlayer counter= " + counter + "is staring again.");

	}

	private boolean getInterrupted() {
		// TODO Auto-generated method stub
		return false;
	}
}
