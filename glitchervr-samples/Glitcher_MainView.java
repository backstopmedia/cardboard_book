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

import javax.microedition.khronos.egl.EGLConfig;

import net.dystopiazero.theglitcher.effects.Glitcher_AssetTestEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_BasicEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_ColorInverseEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_Effect;
import net.dystopiazero.theglitcher.effects.Glitcher_AppleIIEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_EffectManager;
import net.dystopiazero.theglitcher.effects.Glitcher_LaughingManEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_PredatorEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_SelectiveColorEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_SobelEdgeEffect;
import net.dystopiazero.theglitcher.effects.Glitcher_Super8Effect;
import net.dystopiazero.theglitcher.effects.Glitcher_VHSGlitchEffect;
import net.dystopiazero.theglitcher.notifications.Glitcher_BitmapNotification;
import net.dystopiazero.theglitcher.notifications.Glitcher_NotificationScheduler;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

public class Glitcher_MainView extends CardboardActivity implements Glitcher_SurfaceListener
{
	private Glitcher_CamSurface surf;
	private FrameLayout layout;
	private static boolean restored=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Log.d(Glitcher_Utils.LOG_TAG, "** Program Startup **");
		initMainWindow();
		initSubsystems();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		if (surf!=null) 
			surf.onResume();
		
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		cam.start(surf,getApplicationContext());
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		if (surf!=null) 
			surf.onPause();
		
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		cam.stop();
		
		//savePrefs();
		
		restored=false;
	}
	
	private void savePrefs()
	{
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();

		if (em.getActiveEffect()!=null)
			Glitcher_Utils.savePreference(Glitcher_Utils.SAVE_LAST_EFFECT, em.getActiveEffect().getEffectShortName(), this);
		
		String vrmode=surf.isVRMode()?"true":"false";
		Glitcher_Utils.savePreference(Glitcher_Utils.SAVE_LAST_MODE, vrmode, this);
		
		int cam_index=cam.getActiveCamIndex();
		Glitcher_Utils.savePreference(Glitcher_Utils.SAVE_LAST_CAM_INDEX,  ""+cam_index, this);
	}
	
	private void restorePrefs()
	{
		if (restored)
			return;
		
		Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		
		String effect=Glitcher_Utils.getPreference(Glitcher_Utils.SAVE_LAST_EFFECT, this);
		String mode=Glitcher_Utils.getPreference(Glitcher_Utils.SAVE_LAST_MODE, this);
		String cam_index_string=Glitcher_Utils.getPreference(Glitcher_Utils.SAVE_LAST_CAM_INDEX, this);
		int cam_index=0;
	
		if (!effect.matches("null"))
			em.setActiveEffect(effect);
		
		if ((mode.matches("true") && !surf.isVRMode()) || (mode.matches("false") && surf.isVRMode()))
				surf.toggleVRMode();
		
		if (cam_index_string!=null && !cam_index_string.matches("null"))
			cam_index=Integer.parseInt(cam_index_string);
		
		//cam.activateCamAtIndex(surf, this, cam_index);
		
		restored=true;
	}
	
	private void initMainWindow()
	{
		Log.d(Glitcher_Utils.LOG_TAG, "Glitcher_MainView,initMainWindow: Initializing window...");
		
		setContentView(R.layout.activity_glitcher_cam_view);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		surf=(Glitcher_CamSurface)findViewById(R.id.effect_surface);
		
		if (surf==null)
		{
			Log.e(Glitcher_Utils.LOG_TAG,"Glitcher_MainView,initMainWindow: could not create GL Surface.");
			return;
		}
		
		surf=(Glitcher_CamSurface)findViewById(R.id.effect_surface);
		//surf.getHolder().addCallback(this);
		surf.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		surf.setZOrderMediaOverlay(true);
		surf.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		surf.addSurfaceListener(this);
		
		setCardboardView(surf);
		
		layout=(FrameLayout)findViewById(R.id.main_layout);
				
		layout.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
				em.next();
				
				Glitcher_NotificationScheduler ns=Glitcher_NotificationScheduler.getInstance();
				ns.clear();
				Glitcher_BitmapNotification bn=new Glitcher_BitmapNotification(surf.getActivityContext(),em.getActiveEffect().getNotificationID(), 2500.0);
				ns.add(bn);
			}
		});
	}
	
	private void initSubsystems()
	{
		Glitcher_Speech speech=Glitcher_Speech.getInstance();
		speech.init(getApplicationContext(),surf);
	}

	@Override
	public void onCardboardTrigger()
	{
		Glitcher_Inputs inputs=Glitcher_Inputs.getInstance();
		inputs.handleMagnetEvent();
	}
	
	private void initEffects()
	{
		Log.d(Glitcher_Utils.LOG_TAG, "Glitcher_MainView,initEffects: Initializing effects...");
		
		Glitcher_EffectManager em=Glitcher_EffectManager.getInstance();
		
		Glitcher_Effect e=new Glitcher_BasicEffect();
		em.add(e);
		
		e=new Glitcher_SobelEdgeEffect();
		em.add(e);
		
		e=new Glitcher_ColorInverseEffect();
		em.add(e);
		
		e=new Glitcher_SelectiveColorEffect();
		em.add(e);
		
		e=new Glitcher_AppleIIEffect();
		em.add(e);
		
		e=new Glitcher_VHSGlitchEffect();
		em.add(e);
		
		e=new Glitcher_LaughingManEffect();
		em.add(e);
		
		e=new Glitcher_Super8Effect();
		em.add(e);
		
		e=new Glitcher_PredatorEffect();
		em.add(e);
	
		//em.dump();
	}
	
	public void onSurfaceHolderChanged(SurfaceHolder holder, int width, int height) 
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_MainView,onSurfaceHolderChanged: Cam Surface changed");
	}

	public void onSurfaceHolderCreated(SurfaceHolder holder) 
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_MainView,onSurfaceHolderCreated: Cam Surface created");
	}

	public void onSurfaceHolderDestroyed(SurfaceHolder holder) 
	{
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_MainView,onSurfaceHolderDestroyed: Cam Surface destroyed");
		
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				cam.stop();
				
				savePrefs();
			}
		});
	}

	public void onSurfaceInitialized(SurfaceHolder holder)
	{
		initEffects();
		
		//Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
		//cam.start(surf,getApplicationContext());
		
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				cam.start(surf,getApplicationContext());
				
				//Glitcher_Speech speech=Glitcher_Speech.getInstance();
				//speech.init(getApplicationContext(),surf);		
			}
		});
		
		restorePrefs();
	}
	
	public void onSurfaceChanged(SurfaceHolder holder)
	{	
	}
	
	public void onSurfaceCreated(SurfaceHolder holder)
	{
		
	}
	
	public void onSurfaceDestroyed(SurfaceHolder holder)
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Glitcher_CamManager cam=Glitcher_CamManager.getInstance();
				cam.stop();
				
				//savePrefs();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int code, KeyEvent event)
	{
		Glitcher_Inputs inputs=Glitcher_Inputs.getInstance();
		return inputs.handleKeyInput(code, event, surf)?true:super.onKeyDown(code, event);
	}
	
	@Override
	public boolean onKeyLongPress(int code, KeyEvent event)
	{
		Log.d(Glitcher_Utils.LOG_TAG,"long-key down event ");
		
		return true;
	}
}
