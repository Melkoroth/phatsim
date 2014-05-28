package phat.body.control.parkinson;

import com.jme3.animation.Bone;
import com.jme3.animation.SkeletonControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 * It generate a trembling in the head of a character.
 *
 * Depends on <b>SkeletonControl</b>
 *
 * @author pablo
 */
public class HeadTremblingControl extends AbstractControl {

    SkeletonControl skeletonControl;
    Bone neck;
    Vector3f position = new Vector3f();
    Quaternion rotation = new Quaternion();
    float[] angles = new float[3];
    int index = 2;
    float minAngle = -FastMath.QUARTER_PI / 4f;
    float maxAngle = FastMath.QUARTER_PI / 4f;
    float angular = FastMath.HALF_PI;
    boolean min = true;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial != null) {
            skeletonControl = spatial.getControl(SkeletonControl.class);
            if (skeletonControl != null) {
                neck = skeletonControl.getSkeleton().getBone("Neck");
                rotation = new Quaternion();
            }
        } else {
            resetHead();
            skeletonControl = null;
            rotation = null;

        }
    }

    private void resetHead() {
        if (neck != null) {
            setUserControlFrom(neck, true);
            neck.getCombinedTransform(position, rotation);
            angles[0] = 0;
            angles[1] = 0;
            angles[2] = 0;
            rotation.fromAngles(angles);
            neck.setUserTransforms(position, rotation, Vector3f.UNIT_XYZ);
            updateBonePositions(neck);
        }
    }

    @Override
    protected void controlUpdate(float fps) {
        if (neck != null) {
            setUserControlFrom(neck, true);
            neck.getCombinedTransform(position, rotation);
            updateRotation(rotation, fps);
            neck.setUserTransforms(position, rotation, Vector3f.UNIT_XYZ);
            updateBonePositions(neck);
            //setUserControlFrom(neck, false);
        }
    }

    private void updateRotation(Quaternion rotation, float tpf) {
        rotation.toAngles(angles);

        //System.out.println("Angles = "+angles[0]+", "+angles[1]+", "+angles[2]);
        float angle = angles[index];

        if (min) {
            if (angle < minAngle) {
                min = false;
            } else {
                angle -= angular * tpf;
            }
        } else {
            if (angle > maxAngle) {
                min = true;
            } else {
                angle += angular * tpf;
            }
        }
        angles[0] = 0;
        angles[1] = 0;
        angles[index] = angle;

        rotation.fromAngles(angles);
    }

    private void updateBonePositions(Bone bone) {
        Transform t = new Transform();
        for (Bone b : bone.getChildren()) {
            t = b.getCombinedTransform(bone.getModelSpacePosition(), bone.getModelSpaceRotation());
            b.setUserTransformsWorld(t.getTranslation(), b.getModelSpaceRotation());
            updateBonePositions(b);
        }
    }

    private void setUserControlFrom(Bone bone, boolean userControl) {
        bone.setUserControl(userControl);
        for (Bone b : bone.getChildren()) {
            setUserControlFrom(b, userControl);
        }
    }

    public float getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(float minAngle) {
        this.minAngle = minAngle;
    }

    public float getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(float maxAngle) {
        this.maxAngle = maxAngle;
    }

    public float getAngular() {
        return angular;
    }

    public void setAngular(float angular) {
        this.angular = angular;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial sptl) {
        HeadTremblingControl control = new HeadTremblingControl();
        control.setSpatial(sptl);
        control.setAngular(angular);
        control.setMaxAngle(maxAngle);
        control.setMinAngle(minAngle);
        return control;
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(angular, "angular", FastMath.PI);
        oc.write(maxAngle, "maxAngle", FastMath.QUARTER_PI / 4f);
        oc.write(minAngle, "minAngle", -FastMath.QUARTER_PI / 4f);
        
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        angular = ic.readFloat("angular", FastMath.PI);
        maxAngle = ic.readFloat("maxAngle", FastMath.QUARTER_PI / 4f);
        minAngle = ic.readFloat("minAngle", -FastMath.QUARTER_PI / 4f);
    }
}