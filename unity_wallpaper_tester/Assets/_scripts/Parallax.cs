using UnityEngine;
using System.Collections;

[AddComponentMenu("Scripts/Puvo/Parallax")]
public class Parallax : MonoBehaviour
{
    [SerializeField]
    private float _layerOffsetFactor = 1.0f;
    public float layerOffsetFactor { set { _layerOffsetFactor = value; } }

    [SerializeField]
    private Transform _target;
    public Transform target { set { _target = value; } }

    Vector3 startPos;

    void Start()
    {
        startPos = _target.position;
    }

    void Update()
    {
        Vector3 pos = transform.position;
        pos.x = -(_layerOffsetFactor - 1f) * (_target.position.x - startPos.x);
        transform.position = pos;
    }
}
