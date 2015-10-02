using UnityEngine;

[RequireComponent(typeof(InteractionManager))]
public class Interactable : MonoBehaviour {

	private static Interactable _current = null;
	private static bool tracecontrol = false;

	[Tooltip("Max distance to be looked from.")]
	[Range(0,100)]
	public float distance = 3f;	

	[Tooltip("Send raycast every? (in ms)")]
	[Range(0,1000)]
	public long sampdelay = 60;

	private long cms, ms = 0, mslook = 0;
	private bool isLookedAt = false, control = false;
	private Collider _collider; // local collider
	private Renderer _renderer; // local renderer
	private Camera _camera; // player camera
	private InteractionManager _im;

	protected RaycastHit hit;

	// A GameObject is no more requesting control
	public static bool releaseControl(Interactable io) {
		if (_current == io) {
			_current = null; 
			io._release ();
			return true;
		} else {
			return false;
		}
	}

	// A GameObject is requesting control
	public static bool requestControl(Interactable io) {
		if (_current == null || !_current.isActiveAndEnabled) {
			_current = io;
			return true;
		// Give control to the nearest requesting GameObject
		} else if (_current.hit.distance > io.hit.distance) {
			Interactable.releaseControl(_current);
			_current = io;
			return true;
		} else {
			if (tracecontrol) Debug.Log(
				"Cannot give control to " + io + " " + _current + " has control");
			return false;
		}
	}

	// Init.
	void Awake() {
		_collider = GetComponent<Collider> ();
		_renderer = GetComponent<Renderer> ();
		_im = GetComponent<InteractionManager> ();
		_camera = GameObject.FindWithTag ("Player").GetComponentInChildren<Camera> ();
		_im.onAwake ();
	}

	// Is player gazing at me?
	private bool raycast() {
		return _collider.Raycast (
			new Ray(_camera.transform.position, _camera.transform.forward)
			, out hit, distance);
	}
	
	void Start() {
		_im.onStart ();
	}

	void Update() {
		// A collider is needed to detected gaze hits
		if (_collider == null) {
			if (_im.ready) {
				
				_collider = GetComponent<Collider>();
				
				if (_collider == null) {
					Debug.LogWarning(this + ": collider is missing!");
					_im.ready = false; 
				}
			}
			return;
		}
		
		bool p = _im.ready;
		_im.onUpdate ();

		if (p != _im.ready && tracecontrol)
			Debug.Log (this + " state changed to ready=" + _im.ready);
		
		if (!_im.ready) {
			if (_current == this) Interactable.releaseControl(this);
			return;
		}

		// It is not necessary to throw raycast at every frame
		cms = Mathf.RoundToInt (Time.fixedTime * 1000);
		if (!(cms - ms > sampdelay)) return;
		
		// reset countdown
		ms = cms;
		
		// Is there something to be visible?		
		if (_renderer != null && _renderer.enabled) {
			if (_renderer.isVisible) {
				isLookedAt = raycast();
				_im.onVisible();
			} else {
				isLookedAt = false;
			}
		} else isLookedAt = raycast();

		if (isLookedAt) {
			if (! control && Interactable.requestControl (this)) {
				if (tracecontrol)
					Debug.Log (this + " got control");
				_im.onControl ();
				_control ();
			}
		} else if (control) Interactable.releaseControl (this);

		if (control) {
			mslook+=sampdelay;
			_im.onLook(mslook);
		}
	}
		
	private void _control() {
		control = true;
		mslook = 0;
	}
	
	private void _release() {
		if (tracecontrol) Debug.Log (this + " released");
		control = false;
		mslook = 0;
		_im.onRelease();
	}

}
