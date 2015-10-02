using UnityEngine;

public class APIProxy {

	public static void CardbboardUpdateState() {
		Cardboard.SDK.UpdateState ();
	}

	public static Quaternion CardbboardRotation() {
		return Cardboard.SDK.HeadPose.Orientation;
	}
	
	public static Vector3 CardbboardPosition() {
		return Cardboard.SDK.HeadPose.Position;
	}
}
