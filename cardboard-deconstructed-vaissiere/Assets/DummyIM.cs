using UnityEngine;

public class DummyIM : InteractionManager {

	public override void onStart () {}

	public override void onUpdate () {}

	public override void onLook (long ms) {
		Debug.Log ("You are looking at me");
	}

	public override void onVisible () {
		Debug.Log ("You can see me");
	}
	
	public override void onControl () {
		Debug.Log ("I have control!");
	}
	
	public override void onRelease () {
		Debug.Log ("I lost control!");
	}

}
