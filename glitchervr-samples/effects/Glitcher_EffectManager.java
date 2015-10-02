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
package net.dystopiazero.theglitcher.effects;

import java.util.Iterator;
import java.util.Vector;

import net.dystopiazero.theglitcher.Glitcher_Utils;

import android.util.Log;

public final class Glitcher_EffectManager 
{
	private static final Glitcher_EffectManager __gem=new Glitcher_EffectManager();
	
	private Vector<Glitcher_Effect> effects;
	private int activeEffect;
	
	private Glitcher_EffectManager()
	{
		effects=new Vector<Glitcher_Effect>();
		activeEffect=0;
	}
	
	public static Glitcher_EffectManager getInstance()
	{
		return __gem;
	}
	
	public Glitcher_Effect getActiveEffect()
	{
		Glitcher_Effect effect=null;
		
		try
		{
			effect=effects.get(activeEffect);
		}
		
		catch(Exception e)
		{
			effect=null;
		}
		
		return effect;
	}
	
	public void setActiveEffect(int index)
	{
		if (index>=effects.size() || index<0)
			return;
		
		activeEffect=index;
	}
	
	public void next()
	{
		activeEffect=(activeEffect+1)%effects.size();
	}
	
	public void prev()
	{
		activeEffect=(effects.size()+activeEffect-1)%effects.size();
	}
	
	public void setActiveEffect(String shortName)
	{
		
		if (shortName==null || shortName.length()<=0)
			return;
		
		shortName=shortName.toLowerCase().trim();
		shortName=shortName.replaceAll("[*]","\\*");
		
		if (shortName.matches("next"))
			next();
		
		else if (shortName.matches("previous"))
			prev();
		
		else
		{
			for(int k=0; k<effects.size(); k++)
			{
				Glitcher_Effect e=effects.get(k);
			
				if (e.getEffectShortName().matches(shortName))
				{
					activeEffect=k;
					break;
				}
			}
		}
	}
	
	public Glitcher_Effect getEffectByName(String name)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			
			if (e.getEffectName().matches(name))
				return e;
		}
		
		return null;
	}
	
	public Glitcher_Effect getEffectByID(int id)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			
			if (e.getEffectID()==id)
				return e;
		}
		
		return null;
	}
	
	public Glitcher_Effect getEffectAt(int index)
	{
		if (index>=0 && index<effects.size())
			return effects.get(index);
		
		else
			return null;
	}
	
	public int size()
	{
		return effects.size();
	}
	
	public boolean add(Glitcher_Effect effect)
	{
		if (effect==null)
			return false;
		
		if (!effect.isReady())
		{
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_EffectManager,add: Building Effect "+effect.getEffectName());
			
			if (!effect.buildEffect())
				return false;
		}
		
		if (contains(effect.getEffectName()))
			return false;
		
		Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_EffectManager,add: Adding new effect: "+effect.getEffectName());
		effects.add(effect);
		
		return true;
	}
	
	public void remove(String name)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			if (e.getEffectName().matches(name))
				i.remove();
		}
	}
	
	public void remove(int id)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			if (e.getEffectID()==id)
				i.remove();
		}
	}
	
	public boolean contains(String effectName)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			if (e.getEffectName().matches(effectName))
				return true;
		}
		
		return false;
	}
	
	public boolean contains(int effectID)
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			if (e.getEffectID()==effectID)
				return true;
		}
		
		return false;
	}
	
	public void clear()
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			e.destroyEffect();
		}
		
		effects.clear();
	}
	
	public void dump()
	{
		for(Iterator<Glitcher_Effect> i=effects.iterator(); i.hasNext();)
		{
			Glitcher_Effect e=i.next();
			Log.d(Glitcher_Utils.LOG_TAG,"Glitcher_EffectManager,dump: Found effect - "+e.getEffectName()+" ("+e.getEffectID()+")");
		}
	}
}
