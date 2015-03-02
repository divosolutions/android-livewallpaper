using UnityEngine;
using UnityEditor;
using System.Collections;
using System.Collections.Generic;
using puvo.utils;

namespace puvo.wallpapercreator
{
    [AddComponentMenu("Puvo/Wallpaper/Wallpaper Creator")]
    public class WallpaperCreator : EditorWindow
    {
        public static string version = "0.1";

        private static int MAX_LAYER = 10;
        private static int MAX_HEIGHT = 2000;
        private int numberOfLayer = 0;
        private int wallpaperWidth = 2000;
        private int wallpaperHeight = 1000;
        private string spritesFolder = "";

        private float moveSlider = 0f;

        private List<LayerElement> layers = new List<LayerElement>(MAX_LAYER);
        [System.Serializable]
        private class LayerElement
        {
            private static float MAX_OFFSET = 4f;
            [SerializeField]
            private int _layer;
            public int layer { get { return _layer; } }

            [SerializeField]
            private float _offsetFactor;
            public float offsetFactor { get { return _offsetFactor; } }

            public LayerElement(int l)
            {
                _layer = l;
                _offsetFactor = 1f;
            }

            public void onGUI()
            {
                EditorGUILayout.LabelField("Layer " + _layer);
                _offsetFactor = Mathf.Clamp(EditorGUILayout.FloatField("Offset Factor", _offsetFactor), 1, MAX_OFFSET);
            }
        }

        [MenuItem("Puvo/Wallpaper/Wallpaper Creator")]
        static void Init()
        {
            EditorWindow.GetWindow<WallpaperCreator>("Wallpaper Creator");
            Debug.Log("init");
        }

        public void OnInspectorUpdate()
        {
            Repaint();
        }

        private void OnGUI()
        {
            GUIStyle gStyle = new GUIStyle(EditorStyles.textField);

            if (layers.Count == 0) {

                for (int i = 0; i < MAX_LAYER; ++i) {
                    layers.Add(new LayerElement(i));
                }
            }

            EditorGUILayout.LabelField("Setup the basic Layout", EditorStyles.largeLabel);
            EditorGUILayout.Space();
            gStyle.normal.textColor = gStyle.active.textColor = gStyle.focused.textColor = System.IO.Directory.Exists("Assets/_sprites/" + spritesFolder) ? Color.black : Color.red;
            spritesFolder = EditorGUILayout.TextField("Sprites Folder", spritesFolder, gStyle);
            EditorGUILayout.Space();
            wallpaperHeight = Mathf.Clamp(EditorGUILayout.IntField("Wallpaper height ", wallpaperHeight), 1, MAX_HEIGHT);
            wallpaperWidth = 2 * wallpaperHeight;
            EditorGUILayout.Space();
            numberOfLayer = Mathf.Clamp(EditorGUILayout.IntField("Number of layers ", numberOfLayer), 1, MAX_LAYER);

            for (int i = 0; i < numberOfLayer; ++i) {
                layers[i].onGUI();
            }

            if (GUILayout.Button("Generate")) {
                Debug.Log("Generate");
                generateLayers();
            }
            EditorGUILayout.Space();
            GameObject mainCamera = GameObject.FindGameObjectWithTag("MainCamera");
            Camera cam = mainCamera.GetComponent<Camera>();
            EditorGUILayout.LabelField("Camera position", EditorStyles.largeLabel);
            float hHalf = (int) (wallpaperHeight / 2);
            float width = (int) (hHalf * cam.aspect);
            moveSlider = EditorGUILayout.Slider(moveSlider, width, wallpaperWidth - width);
            cam.orthographicSize = hHalf;
            cam.farClipPlane = numberOfLayer * wallpaperHeight / 4;

            Vector3 pos = mainCamera.transform.position;
            pos.x = moveSlider;
            pos.y = -hHalf;
            pos.z = -cam.farClipPlane + wallpaperHeight / 8;
            mainCamera.transform.position = pos;

        }

        private void clearLayers()
        {
            GameObject root = GameObject.Find("_root");


            if (root != null) {
                for (int i = 0; i < root.transform.childCount; ) {
                    Transform t = root.transform.GetChild(i);
                    string name = t.gameObject.name;

                    try {
                        int layer = int.Parse(name.Replace("Layer", ""));

                        if (layer >= numberOfLayer) {
                            DestroyImmediate(t.gameObject);
                            continue;
                        }

                        while (t.childCount > 0) {
                            Transform ct = t.GetChild(0);
                            DestroyImmediate(ct.gameObject);
                        }
                    } catch (System.Exception) {
                        DestroyImmediate(t.gameObject);
                        continue;
                    }
                    ++i;
                }
            }
        }

        private void generateLayers()
        {
            GameObject root = GameObject.Find("_root");
            GameObject mainCamera = GameObject.FindGameObjectWithTag("MainCamera");
            Camera cam = mainCamera.transform.GetComponent<Camera>();
            float size = cam.orthographicSize*cam.aspect * 2;
            float defaultOffscreen = wallpaperWidth - size;

            clearLayers();

            if (root == null) {
                root = new GameObject("_root");
            }

            cam.orthographic = true;

            for (int i = 0; i < numberOfLayer; ++i) {
                string layerName = "Layer" + i;
                GameObject layer;

                if (!root.transform.FindChild(layerName)) {
                    layer = new GameObject(layerName);
                    layer.transform.SetParent(root.transform);
                } else {
                    layer = root.transform.FindChild(layerName).gameObject;
                }

                if (!layer.GetComponent<Parallax>()) {
                    layer.AddComponent<Parallax>();
                }

                float diff = wallpaperWidth * layers[i].offsetFactor - size;
                layer.GetComponent<Parallax>().layerOffsetFactor = diff / defaultOffscreen;
                layer.GetComponent<Parallax>().target = mainCamera.transform;

                addSpritesToLayer(i, layer);
            }
        }

        private void addSpritesToLayer(int layer, GameObject layerObject)
        {
            Vector3 pos;
            string[] assetGUIDs = AssetDatabase.FindAssets("", new string[] { "Assets/_sprites/" + spritesFolder });

            foreach (string guid in assetGUIDs) {
                Sprite s = (Sprite) AssetDatabase.LoadAssetAtPath(AssetDatabase.GUIDToAssetPath(guid), typeof(Sprite));
                GameObject spriteObject;

                if (!s.name.StartsWith("r" + layer + "__")) {
                    continue;
                }

                if (layerObject.transform.FindChild(s.name)) {
                    spriteObject = layerObject.transform.FindChild(s.name).gameObject;
                } else {
                    spriteObject = new GameObject(s.name);
                }

                spriteObject.transform.SetParent(layerObject.transform);

                if (!spriteObject.GetComponent<SpriteRenderer>()) {
                    spriteObject.AddComponent<SpriteRenderer>();
                }
                spriteObject.GetComponent<SpriteRenderer>().sprite = s;

                string[] splitName = s.name.Split(new string[] { "__" }, System.StringSplitOptions.RemoveEmptyEntries);
                if (splitName.Length < 9) {
                    continue;
                }
                pos = spriteObject.transform.position;
                pos.x = float.Parse(splitName[7].Replace("m", "-"));
                pos.y = -float.Parse(splitName[8].Replace("m", "-"));
                pos.z = -layer * wallpaperHeight / 4;
                spriteObject.transform.position = pos;

                spriteObject.GetComponent<SpriteRenderer>().sortingOrder = int.Parse(splitName[3]);

            }
        }
    }
}
