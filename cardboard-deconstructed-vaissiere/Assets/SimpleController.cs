using UnityEngine;

public class SimpleController : MonoBehaviour {
	
	private static GameObject player;
	private static Camera playercamera;
	private static bool trace = true;
	private static SimpleController _this = null;
	
	void Awake () {
		
		if (_this != null) Debug.LogError (this + " should be a singleton!");
		_this = this;
		
		player = GameObject.FindWithTag ("Player");
		playercamera = player.GetComponentInChildren<Camera> ();
		if (trace) Debug.Log ("Connected controller to " + 
		                      player + "." + playercamera);
	}
	
	public static void updatePlayer() {
		playercamera.transform.parent.localEulerAngles = 
			new Vector3(0f,AxisManager.getAzimuth(),0f);
		playercamera.transform.localEulerAngles = 
			new Vector3(AxisManager.getElevation(),0f, AxisManager.getTilt());
	}

	Vector3 direction = new Vector3();
	bool walk = false;

	void Update() {

		// Code below is for the sake of this example only
		// Never make recurring calls to GetComponent method
		if (!walk && GetComponent<AxisProcessor> ().islefttilt)
			walk = true;

		if (walk) {
			direction = player.transform.rotation*Vector3.forward;
			player.GetComponent<CharacterController> ().Move (direction * .01f);
		}

	}

}
