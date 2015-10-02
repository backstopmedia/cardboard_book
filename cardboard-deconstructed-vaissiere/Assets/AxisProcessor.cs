using UnityEngine;

public class AxisProcessor : MonoBehaviour {
	
	// Number of events to keep in memory
	private static int samplesize = 6; 
	// Current event position
	private int ibuffer = 0;
	// Events buffers
	private float[] xbuffer = new float[samplesize];
	private float[] ybuffer = new float[samplesize];
	private float[] zbuffer = new float[samplesize];
	
	// Do we need to collect a new sample?
	private bool trackingisdirty = true;
	// FixedUpdate method calls count
	private int fucount = 0;
	// Current readings
	float x,y,z;
	
	
	// FixedUpdate is called at regular intervals
	// (every 20ms by default, check Project Settings>Time)
	void FixedUpdate () {
		
		// Shall we sample? Yes every 120ms.
		// Tune according to your needs.
		if (fucount > 5 && !trackingisdirty) 
			trackingisdirty = true;
		
		fucount++;
	}
	
	// Update is call at every frame
	void Update () {
		
		x = AxisManager.getElevation ();
		y = AxisManager.getAzimuth ();
		z = AxisManager.getTilt ();
		
		// Readability concerns
		// A negative value means below horizon
		if (x > 180) x = 360 - x; else x = -x; 
		// A negative value means left
		if (z > 180) z = 360 - z; else z = -z;
		
		
		if (trackingisdirty) {
			sample (x, y, z);
			trackingisdirty = false;
			fucount=0;
		}
	}
	
	public bool isstarring { get; private set;} 
	public bool islefttilt { get; private set;}
	public bool isnodyes { get; private set;}
	
	private void sample(float x, float y, float z) {
		
		if (++ibuffer >=  samplesize) ibuffer = 0;
		
		xbuffer[ibuffer] = x;
		ybuffer[ibuffer] = y;
		zbuffer[ibuffer] = z;
		
		// True if player is almost not moving is head
		// 1 degree is OK. Adjust according to your need
		isstarring = (astddev(xbuffer) < 1 
		              && astddev(ybuffer) < 1 && astddev(zbuffer) < 1); 
		
		// True if player just tilted is head to the left
		islefttilt = (astddev (xbuffer) < 3 		  // Stable Elevation
		              && astddev (zbuffer) < (169)			  // Max amplitude of 13°
		              && across (zbuffer, -7.5f, ibuffer) == 2);// Back & forth over 7.5°
		
		
		// True if player nods yes
		isnodyes = (astddev (zbuffer) < 3 && astddev (ybuffer) < 5  
		            && astddev (xbuffer) > (4) // +/-2° amplitude
		            && across (xbuffer, aavg (xbuffer), ibuffer) > 2);
	}


	private float sum,avg;
	private int cnt;
	
	// Average of array values
	private float aavg(float[] array) {
		
		sum = 0;
		foreach (float f in array) sum+=f;
		
		return  sum / array.Length;
	}
	
	// Standard deviation
	private float astddev(float[] array) {
		
		avg = aavg(array);
		sum = 0;
		foreach (float f in array) sum+= (f - avg) * (f -avg);
		
		return sum / array.Length;
	}
	
	// Count oscillations around a value
	// "last" is last inserted value index as one has to avoid
	// processing the newest reading with the oldest one
	private int across(float[] array, float value, int last) {
		
		cnt = 0;
		for (int i,j,k = last+1; k < array.Length + last; k++) {
			
			i = k%array.Length;
			j = (k+1) %array.Length;
			
			if ((array[i] < value && array[j] > value) 
			    || (array[i] > value && array[j] < value)) 
				cnt++;
		}
		
		return  cnt;
	}

}