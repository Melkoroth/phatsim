/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package phat.body.control.physics.ragdoll;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author pablo
 */
public class SimulateTripOver extends RagdollTransitionControl implements Control {
    float frictionFactor = 1f;
    boolean tripOver = false;
    
    @Override
    public void initBody() {

        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Head"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Neck"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Jaw"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Spine1"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Spine"));
        //objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("Hips"));

        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("LeftShoulder"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("LeftArm"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("LeftShoulder"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("LeftForeArm"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("LeftHand"));

        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("RightShoulder"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("RightArm"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("RightForeArm"));
        objectsToPush.add(kinematicRagdollControl.getBoneRigidBody("RightHand"));
        
        
        kinematicRagdollControl.getBoneRigidBody("RightToeBase").setFriction(frictionFactor);
        kinematicRagdollControl.getBoneRigidBody("RightFoot").setFriction(frictionFactor);
        kinematicRagdollControl.getBoneRigidBody("RightLeg").setFriction(frictionFactor);
        
        kinematicRagdollControl.getBoneRigidBody("LeftToeBase").setFriction(frictionFactor);
        kinematicRagdollControl.getBoneRigidBody("LeftFoot").setFriction(frictionFactor);
        kinematicRagdollControl.getBoneRigidBody("LeftLeg").setFriction(frictionFactor);
    }

    @Override
    public void applyPhysics(PhysicsSpace ps, float tpf) {
        Vector3f direction = characterControl.getWalkDirection();
        Vector3f speed = direction.divide(characterControl.getPhysicsSpace().getAccuracy());
        for (PhysicsRigidBody prb: objectsToPush) {
            prb.setLinearVelocity(direction);
        }
    }

    public float getFrictionFactor() {
        return frictionFactor;
    }

    public void setFrictionFactor(float frictionFactor) {
        this.frictionFactor = frictionFactor;
    }

    public boolean isTripOver() {
        return tripOver;
    }

    public void setTripOver(boolean tripOver) {
        this.tripOver = tripOver;
    }
    
    @Override
    public Control cloneForSpatial(Spatial sptl) {
        SimulateTripOver control = new SimulateTripOver();
        control.setSpatial(sptl);
        control.setFrictionFactor(frictionFactor);
        return control;
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(frictionFactor, "frictionFactor", 1f);
        
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        frictionFactor = ic.readFloat("frictionFactor", 1f);
    }

    @Override
    protected void createSpatialData(Spatial spat) {
        
    }

    @Override
    protected void removeSpatialData(Spatial spat) {
        
    }

    @Override
    protected void setPhysicsLocation(Vector3f vec) {
        
    }

    @Override
    protected void setPhysicsRotation(Quaternion quat) {
        
    }
}
