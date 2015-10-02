using UnityEngine;

public class AxisManager : MonoBehaviour {

	private static Transform unityaxis;
	private static Transform cardbboardaxis;
	private static AxisManager _this = null;
	
	void Awake () {
		
		if (_this != null) Debug.LogError (this + " should be a singleton!");
		_this = this;

		unityaxis = transform.FindChild("AlternateAxis");
		cardbboardaxis = transform.FindChild("CardboardAxis"); 
	}


	// <-> Azimuth (or Yaw)
	public static float getAzimuth() {
		return (unityaxis.rotation.eulerAngles.y + cardbboardaxis.eulerAngles.y)%360;
	}
	
	// | Elevation (or pitch)
	public static float getElevation() {
		return (unityaxis.rotation.eulerAngles.x + cardbboardaxis.eulerAngles.x)%360;
	}
	
	// O tilt (or roll)
	public static float getTilt() {
		return (unityaxis.rotation.eulerAngles.z + cardbboardaxis.eulerAngles.z)%360;
	}

}
