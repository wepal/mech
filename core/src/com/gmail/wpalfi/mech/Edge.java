package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.ArrayList;
import java.util.List;

public class Edge implements Drawable {
    private Camera _camera;
    private World _world;
    private Node _node1,_node2;
    private float _restLength;
    private float _strength = 1;
    private Color _color;
    private DistanceJoint _distanceJoint;
    private Body _body1, _body2;
    private Fixture _fixture1, _fixture2;
    private RevoluteJoint _revoluteJoint1, _revoluteJoint2;
    private PrismaticJoint _prismaticJoint;
    private float _lastUpdateLength;
    private List<Drive> _drives=new ArrayList<Drive>();
    //cached
    private Vector2 pos1,pos2,dir,start,end;
    private float _length;

    public Edge(Camera camera, World world, Node node1, Node node2, Color color){
        _camera=camera;
        _world=world;
        _node1=node1;
        _node2=node2;
        _color=color;

        DistanceJointDef distJointDef = new DistanceJointDef();
        Vector2 p1=_node1.getBody().getPosition();
        Vector2 p2=_node2.getBody().getPosition();
        distJointDef.initialize(_node1.getBody(), _node2.getBody(), p1, p2);
        distJointDef.dampingRatio=1;
        distJointDef.frequencyHz=10f;//2f;
        _distanceJoint = (DistanceJoint) world.createJoint(distJointDef);
        _restLength = distJointDef.length;

        float dist = p1.dst(p2);
        Vector2 axis = new Vector2(p2).sub(p1).setLength(1);
        Vector2 center = new Vector2(p1).add(p2).scl(.5f);
        float angle = axis.angle()*MathUtils.degreesToRadians;

        BodyDef bd1 = new BodyDef();
        bd1.position.set(p1);
        bd1.angle = angle;
        _body1 = world.createBody(bd1);
        BodyDef bd2 = new BodyDef();
        bd2.position.set(p2);
        bd2.angle = angle+MathUtils.PI;
        _body2 = world.createBody(bd2);
        BodyDef.BodyType bodyType = _color==Color.WHITE ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        _body1.setType(bodyType);
        _body2.setType(bodyType);

        RevoluteJointDef revoluteJointDef1 = new RevoluteJointDef();
        revoluteJointDef1.initialize(node1.getBody(), _body1, node1.getBody().getPosition());
        revoluteJointDef1.collideConnected=false;
        _revoluteJoint1=(RevoluteJoint)world.createJoint(revoluteJointDef1);

        RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();
        revoluteJointDef2.initialize(node2.getBody(), _body2, node2.getBody().getPosition());
        revoluteJointDef2.collideConnected=false;
        _revoluteJoint2=(RevoluteJoint)world.createJoint(revoluteJointDef2);

        PrismaticJointDef prismaticJointDef=new PrismaticJointDef();
        prismaticJointDef.initialize(_body1,_body2,center,axis);
        _prismaticJoint=(PrismaticJoint)world.createJoint(prismaticJointDef);


        update();
    }

    private void updateLength(){
        if(_drives.isEmpty()){
            return;
        }
        float lengthSum=0;
        for (Drive drive : _drives) {
            float pos = drive.slide.getPosition();
            float length = drive.length * pos * _restLength;
            lengthSum += length;
        }
        float meanLength = lengthSum/_drives.size();
        _distanceJoint.setLength(meanLength);
    }

    private void updateFixtures(){
        if(_fixture1!=null){
            _body1.destroyFixture(_fixture1);
            _body2.destroyFixture(_fixture2);
        }
        float w=_length*.75f;
        Vector2[] vertices=new Vector2[4];
        vertices[0]=new Vector2(0,-.1f);
        vertices[1]=new Vector2(0,+.1f);
        vertices[2]=new Vector2(w,+.1f);
        vertices[3]=new Vector2(w,-.1f);

        PolygonShape shape1 = new PolygonShape();
        shape1.set(vertices);
        PolygonShape shape2 = new PolygonShape();
        shape2.set(vertices);

        FixtureDef fdef1 = new FixtureDef();
        fdef1.shape = shape1;
        fdef1.density = 1.0f;
        fdef1.friction = 0.3f;
        fdef1.restitution = 0.6f;
        fdef1.filter.categoryBits = 0x0002;
        fdef1.filter.maskBits = 0x0001;
        _fixture1 = _body1.createFixture(fdef1);

        FixtureDef fdef2 = new FixtureDef();
        fdef2.shape = shape2;
        fdef2.density = 1.0f;
        fdef2.friction = 0.3f;
        fdef2.restitution = 0.6f;
        fdef1.filter.categoryBits = 0x0002;
        fdef1.filter.maskBits = 0x0001;
        _fixture2 = _body2.createFixture(fdef2);

        _lastUpdateLength = _length;
    }

    public void update(){
        pos1 = _node1.getBody().getPosition();
        pos2 = _node2.getBody().getPosition();
        dir = new Vector2(pos2).sub(pos1).setLength(1f);
        start = new Vector2(pos1).add(new Vector2(dir).scl(_node1.getRadius()));
        end = new Vector2(pos2).add(new Vector2(dir).scl(-_node2.getRadius()));
        _length = pos1.dst(pos2);

        if(Math.abs((_length- _lastUpdateLength)/ _lastUpdateLength)>.1){
            updateFixtures();
        }

        updateLength();
    }
    public float getRestLength(){
        return _restLength;
    }
    public Node node1(){
        return _node1;
    }
    public Node node2(){
        return _node2;
    }
    public Color color(){
        return _color;
    }
    public void setColor(Color color){
        _color=color;
    }
    @Override
    public float hitTest(Vector2 pos){
        Vector2 d = new Vector2(pos).sub(start);
        float x = d.dot(dir);
        float y = new Vector2(d).sub(new Vector2(dir).scl(x)).len();
        float len = start.dst(end);
        float xdist = Math.max(0-x,x-len);
        xdist=Math.max(0,xdist);
        float ydist = Math.max(y-.1f,-.1f-y);
        ydist=Math.max(0,ydist);
        return (float)Math.sqrt(xdist*xdist+ydist*ydist);
    }
    @Override
    public void render(ShapeRenderer renderer){
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        Util.setRendererColor(renderer, _color);
        renderer.rectLine(start,end,.1f);
        renderer.end();
    }
    public void renderSelection(ShapeRenderer renderer){
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(.2f,.2f,.2f,1);
        float margin =  .3f * worldMeterPerScreenCm();
        renderer.rectLine(start,end,.1f+2*margin);
        renderer.end();
    }
    public void renderDrive(ShapeRenderer renderer, boolean selected) {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        if(selected){
            renderer.setColor(.4f,.4f,1,1);
        }else{
            renderer.setColor(.1f,.1f,.5f,1);
        }
        float margin =  .3f * worldMeterPerScreenCm();
        renderer.rectLine(start,end,.1f+2*margin);
        renderer.end();
    }
    //TODO: remove?
    public void addDrive(Drive drive) {
        _drives.add(drive);
    }

    public boolean hasDrive(Slide slide) {
        for (Drive drive : _drives) {
            if(drive.slide==slide){
                return true;
            }
        }
        return false;
    }
    public boolean hasDrive(Iterable<Slide> slides) {
        for (Slide slide : slides) {
            if (hasDrive(slide)) {
                return true;
            }
        }
        return false;
    }
    public Drive getDrive(Slide slide) {
        for (Drive drive : _drives) {
            if(drive.slide==slide){
                return drive;
            }
        }
        return null;
    }
    private float pixPerWorldMeter() {
        return Gdx.graphics.getWidth() / _camera.viewportWidth;
    }
    private float worldMeterPerScreenCm() {
        float pixPerScreenCm = Gdx.graphics.getPpcX();
        return pixPerScreenCm / pixPerWorldMeter();
    }
}