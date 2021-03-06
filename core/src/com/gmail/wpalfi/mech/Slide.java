package com.gmail.wpalfi.mech;

/**
 * Created by wpalfi on 07.02.17.
 */

import java.util.List;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class Slide implements Drawable{
    private OrthographicCamera _camera;
    private Vector2 _start, _end;
    private float _home;
    private Body _body;
    private Body _homeBody;
    private DistanceJoint _pullBackJoint;
    private PrismaticJoint _prismaticJoint;
    private World _world;
    private MouseJoint _mouseJoint;

    public Slide(OrthographicCamera camera, World world, Vector2 pos, float home) {
        _camera=camera;
        _world=world;
        _home=home;
        _start = new Vector2(pos.add(0,5));
        _end = new Vector2(pos.add(0,-5));

        Vector2 dist=new Vector2(_end).sub(_start);
        Vector2 homepos=new Vector2(dist).scl(home).add(_start);

        CircleShape sd = new CircleShape();
        sd.setRadius(1);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 1.0f;
        fdef.friction = 0.3f;
        fdef.restitution = 0.6f;

        BodyDef bd = new BodyDef();
        // bd.isBullet = true;
        //bd.linearDamping = 1f;
        bd.fixedRotation = true;
        bd.allowSleep = true;
        bd.position.set(homepos);
        Body body = world.createBody(bd);
        body.createFixture(fdef);
        body.setType(BodyDef.BodyType.DynamicBody);
        _body = body;

        BodyDef homeBodyDef = new BodyDef();
        homeBodyDef.allowSleep = true;
        homeBodyDef.position.set(homepos);
        Body homeBody = world.createBody(homeBodyDef );
        homeBody.setType(BodyDef.BodyType.StaticBody);
        _homeBody=homeBody;

        PrismaticJointDef prismaticJointDef=new PrismaticJointDef();
        prismaticJointDef.initialize(homeBody,body,homeBody.getPosition(),new Vector2(0,1));
        prismaticJointDef.enableLimit=true;
        prismaticJointDef.lowerTranslation=-2.5f;
        prismaticJointDef.upperTranslation=2.5f;
        _prismaticJoint=(PrismaticJoint)world.createJoint(prismaticJointDef);

        createPullBackJoint();
    }

    private void createPullBackJoint(){
        DistanceJointDef pullBackJointDef = new DistanceJointDef();
        pullBackJointDef.dampingRatio=3;
        pullBackJointDef.frequencyHz=3;
        pullBackJointDef.bodyA= _body;
        pullBackJointDef.bodyB= _homeBody;
        pullBackJointDef.length=0;
        _pullBackJoint = (DistanceJoint)_world.createJoint(pullBackJointDef);
    }

    public float getPosition(){
        Vector2 pos = new Vector2(_body.getPosition());
        pos.sub(_start);
        Vector2 maxVec=new Vector2(_end).sub(_start);
        float p = pos.dot(maxVec)/maxVec.len2();
        return p - _home; //[-1,+1]
    }

    public void render(ShapeRenderer renderer) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        float width = .1f;
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(.5f,.5f,1f,.4f);

        renderer.rectLine(_start,_end,width);
        renderer.circle(_start.x,_start.y,width/2,64);
        renderer.circle(_end.x,_end.y,width/2,64);

        Vector2 pos = _body.getPosition();

        renderer.circle(pos.x,pos.y,1,64);
        renderer.end();
    }

    public float hitTest(Vector2 mousePos) {
        Vector2 pos = _body.getPosition();
        float radius=1f;
        float dist = pos.dst(mousePos)-radius;
        return dist;
    }

    public void touchDown(Vector2 mousePix) {
        Vector2 pos=Util.unproject(_camera,mousePix);
        MouseJointDef defJoint = new MouseJointDef();
        defJoint.maxForce = 10000000 * _body.getMass();
        //defJoint.dampingRatio=1;
        //defJoint.frequencyHz=1000f;
        defJoint.bodyA = _homeBody;
        defJoint.bodyB = _body;
        defJoint.target.set(pos.x, pos.y);
        _mouseJoint = (MouseJoint) _world.createJoint(defJoint);
        _world.destroyJoint(_pullBackJoint);
        _pullBackJoint=null;
    }

    public void touchUp(Vector2 mousePix) {
        _world.destroyJoint(_mouseJoint);
        _mouseJoint=null;
        createPullBackJoint();
    }

    public void touchDragged(Vector2 mousePix){
        Vector2 pos=Util.unproject(_camera,mousePix);
        _mouseJoint.setTarget(pos);
    }

    public boolean isDragging(){
        return _mouseJoint!=null;
    }
    public void destroy(){
        _world.destroyBody(_body);
        _world.destroyBody(_homeBody);
    }

}
