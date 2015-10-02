/*The MIT License (MIT)

Copyright (c) 2015 Mike Pasamonik

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/
package net.dystopiazero.theglitcher;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import net.dystopiazero.theglitcher.effects.Glitcher_EffectManager;
import net.dystopiazero.theglitcher.notifications.Glitcher_BitmapNotification;
import net.dystopiazero.theglitcher.notifications.Glitcher_NotificationScheduler;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class Glitcher_Speech implements RecognitionListener 
{
	private static final Glitcher_Speech __speech=new Glitcher_Speech();
	private Context context;
	private SpeechRecognizer recog;
	private Glitcher_CamSurface surf;
	private boolean isInit;
	
	
	private Glitcher_Speech()
	{
		isInit=false;
	}
	
	public static Glitcher_Speech getInstance()
	{
		return __speech;
	}
	
	public void init(Context context, Glitcher_CamSurface surf)
	{
		recog=SpeechRecognizer.createSpeechRecognizer(context);
		recog.setRecognitionListener(this);
		
		this.surf=surf;
		
		this.context=context;
		
		isInit=true;
	}
	
	public void activateSpeech()
	{
		if (!isReady())
		{
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Speech,activateSpeech: Speech recog engine not ready!");
		}
		
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		Glitcher_Cam camera=cam.getActiveCam();
		
		if (camera!=null && camera.isRecording())
		{
			camera.stopVideoRecord();
			
			Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
			ns.clear();
			Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.recordstop, 3000.0);
			ns.add(bn);
		}
		
		else
		{
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			//intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
			recog.startListening(intent);
		}
	}
	
	public boolean isReady()
	{
		return isInit;
	}
	

	@Override
	public void onResults(Bundle results) 
	{
		ArrayList<String> found=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		
		for(Iterator<String> i=found.iterator(); i.hasNext();)
		{
			String result=i.next();
			Log.d(Glitcher_Utils.LOG_TAG,"Found speech result: "+result);
		
			if (result==null)
				continue;
			
			else
				result=result.toLowerCase().trim();
			
			if (result.startsWith("effect") || result.startsWith("next") || result.startsWith("back") || result.startsWith("previous"))
			{
				String filter="";
	
				if (result.startsWith("next"))
					filter="next";
				else if (result.startsWith("previous") || result.startsWith("back"))
					filter="previous";
				else
					filter=result.substring(6).trim();
				
				Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Speech,Filter: "+filter);
				
				if (filter!=null && filter.length()>0)
					em.setActiveEffect(filter);
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				ns.clear();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),em.getActiveEffect().getNotificationID(), 2500.0);
				ns.add(bn);
				
				return;
			}				
			
			else if (result.startsWith("frame"))
			{
				surf.takeScreenshot();
				
				return;
			}
			
			else if (result.matches("camera next"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				cam.activateNext(surf,context);
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.nextcamera, 3000.0);
				ns.add(bn);
				
				return;
			}
			
			else if (result.matches("camera previous"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				cam.activatePrevious(surf,context);
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.previouscamera, 3000.0);
				ns.add(bn);
				
				return;
			}
			
			else if (result.matches("torch on"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null)
					camera.enableTorch();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.torchon, 3000.0);
				ns.add(bn);
				
				return;
			}
			
			else if (result.matches("torch off"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null)
					camera.disableTorch();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.torchoff, 3000.0);
				ns.add(bn);
				
				return;
			}
			
			else if (result.startsWith("photo"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null)
					camera.takePicture();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.photocaptured, 3000.0);
				ns.add(bn);
				
				return;
			}
			
			else if (result.startsWith("video"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null && !camera.isRecording())
				{
					camera.startVideoRecord();
					
					Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
					ns.clear();
					Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.recordstart, 3000.0);
					ns.add(bn);
				}
				
				return;
			}
			
			/*else if (result.toLowerCase().startsWith("video end"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null && camera.isRecording())
				{
					camera.stopVideoRecord();
					
					Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
					Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),R.drawable.recordstop, 3000.0);
					ns.add(bn);
				}
				
				return;
			}*/
			
			else if (result.startsWith("focus"))
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				Glitcher_Cam camera=cam.getActiveCam();
				
				if (camera!=null)
					camera.focus();	
				
				return;
			}
		}
	}

	@Override
	public void onPartialResults(Bundle partialResults) 
	{
	}

	@Override
	public void onEvent(int eventType, Bundle params) 
	{
	}
	
	@Override
	public void onReadyForSpeech(Bundle params) 
	{
	}

	@Override
	public void onBeginningOfSpeech() 
	{
	}

	@Override
	public void onRmsChanged(float rmsdB) 
	{
	}

	@Override
	public void onBufferReceived(byte[] buffer) 
	{
	}

	@Override
	public void onEndOfSpeech()
	{
	}

	@Override
	public void onError(int error) 
	{
		if (error==SpeechRecognizer.ERROR_NETWORK || 
				error==SpeechRecognizer.ERROR_NETWORK_TIMEOUT || 
				error==SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
		{
			
			recog.cancel();
		}
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_Speech,onError: "+error);
	}
}
