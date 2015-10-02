using UnityEngine;
using System.Collections;

public class CardboardAxisControl : MonoBehaviour {

	private static CardboardAxisControl _this = null;
	
	void Awake() {
		if (_this == null) _this = this;
		
		if (_this != this) {
			Debug.LogWarning ("this should be a singleton.");
			return;
		}
	}

	// Call this function first whenever you wan't to update 
	// another transform in regard to head position.
	public static void RequestHeadUpdate() {
		_this.UpdateHead();
	}

	private bool updated;
	
	void Update() { updated = false; } // One update per frame

	void LateUpdate() { UpdateHead(); } // By default refresh head position here
	
	// Compute new head pose.
	private void UpdateHead() {

		if (updated) return; 

		updated = true;
		APIProxy.CardbboardUpdateState();
		transform.localRotation = APIProxy.CardbboardRotation();

		SimpleController.updatePlayer(); 
	}
}
