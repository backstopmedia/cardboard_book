using UnityEngine;

public abstract class InteractionManager : MonoBehaviour {
	
	public bool ready { get; set;} 
	
	public virtual void onAwake() {
		ready = true;
	}
	
	// Called once
	public abstract void onStart ();
	
	// Called once per frame
	public abstract void onUpdate ();
	
	// Called once per frame if it is 
	// in sight of the player
	public abstract void onLook (long ms);
	
	// Called once per frame if it is on screen
	public abstract void onVisible ();
	
	public abstract void onControl ();
	
	public abstract void onRelease ();
	
}