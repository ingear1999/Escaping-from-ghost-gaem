import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.control.CameraControl;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.collision.CollisionResults;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class EscapingGhost extends SimpleApplication {

    //=============== Player Movement Flags ===============
    private boolean goForward = false;
    private boolean goBackward = false;

    //=============== Player Node ===============
    private Node player;

    //=============== Speed ===============
    private float moveSpeed = 5f;

    public static void main(String[] args) {
    	 EscapingGhost app = new  EscapingGhost();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //camera is controlled manually
        flyCam.setEnabled(false);

        //=============== Player ===============
        player = new Node("Player");
        Geometry playerGeom = createBox("Player", 0.5f, 1f, 0.5f, ColorRGBA.Blue);
        player.attachChild(playerGeom);
        rootNode.attachChild(player);

        // set player starting position
        player.setLocalTranslation(0, 1f, 0);

        //=============== Floor and Wall ===============
        Geometry wall = makeWall();  // wall
        Geometry floor = makeFloor(); // floor
        rootNode.attachChild(wall);
        rootNode.attachChild(floor);

        //=============== Camera ===============
        ChaseCamera chaseCam = new ChaseCamera(cam, player, inputManager);
        chaseCam.setDefaultDistance(6);
        chaseCam.setLookAtOffset(new Vector3f(0, 1, 0)); // focus on player's head
        chaseCam.setRotationSpeed(2f);
        chaseCam.setMinVerticalRotation(-FastMath.HALF_PI / 4);
        chaseCam.setMaxVerticalRotation(FastMath.HALF_PI / 2);
        chaseCam.setDefaultHorizontalRotation(0);
        chaseCam.setTrailingEnabled(true);

        //=============== Input ===============
        initKeys();
    }

    //=============== Wall ===============
    public Geometry makeWall() {
        for (int x = 0; x <= 20; x++) {
            Box box = new Box(1, 0.1f, 0.1f); // size
            Geometry wallBlock = new Geometry("WallBlock_" + x, box);

            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.Brown);
            wallBlock.setMaterial(mat);

            float i = x * 2f - 19.1f; // spacing by 2 units
            wallBlock.setLocalTranslation(i, 0.1f, -5);

            rootNode.attachChild(wallBlock); // attach each block!
        }

        // return one dummy block just to fulfill the method
        return new Geometry();
    }

    //=============== Floor ===============
    public Geometry makeFloor() {
        Box floorBox = new Box(20, 0.1f, 20);
        Geometry floor = new Geometry("Floor", floorBox);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
        floor.setMaterial(mat);

        floor.setLocalTranslation(0, -0.1f, 0);
        return floor;
    }

    //=============== Input Bindings ===============
    private void initKeys() {
        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));

        inputManager.addListener(actionListener, "Forward", "Backward");
    }

    //=============== Action Listener ===============
    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String binding, boolean isPressed, float tpf) {
            if (binding.equals("Forward")) {
                goForward = isPressed;
            } else if (binding.equals("Backward")) {
                goBackward = isPressed;
            }
        }
    };

    //=============== Main Update ===============
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f forwardDir = player.getLocalRotation().mult(Vector3f.UNIT_Z);
        Vector3f walkDirection = Vector3f.ZERO.clone();

        if (goForward) {
            walkDirection = walkDirection.add(forwardDir.mult(moveSpeed * tpf));
        }
        if (goBackward) {
            walkDirection = walkDirection.subtract(forwardDir.mult(moveSpeed * tpf));
        }

        //=============== Collision Detection ===============
        if (!collides(player.getLocalTranslation().add(walkDirection))) {
            player.move(walkDirection);
        }
    }

    //=============== Collision Check ===============
    private boolean collides(Vector3f nextPosition) {
        CollisionResults results = new CollisionResults();
        BoundingBox box = (BoundingBox) player.getWorldBound();

        for (Spatial s : rootNode.getChildren()) {
            if (s.getName() != null && s.getName().startsWith("WallBlock")) {
                s.collideWith(box, results);
                if (results.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    //=============== Helper Method to Create Colored Box ===============
    private Geometry createBox(String name, float x, float y, float z, ColorRGBA color) {
        Box box = new Box(x, y, z);
        Geometry geom = new Geometry(name, box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        return geom;
    }
}
