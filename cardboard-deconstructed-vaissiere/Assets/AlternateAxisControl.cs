using UnityEngine;

public class AlternateAxisControl : MonoBehaviour {

	protected float y;

	void LateUpdate () {

		#if UNITY_EDITOR
		y = Input.GetAxis ("Mouse X");
		#else
		y = Input.GetAxis ("Horizontal"); 
		#endif
		transform.Rotate(0, y, 0);

		SimpleController.updatePlayer();
	}
}
