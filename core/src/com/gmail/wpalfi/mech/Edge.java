package com.gmail.wpalfi.mech;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
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

public class Edge{
    public Edge(World world, Node node1, Node node2, Color color){
        _world=world;
        _node1=node1;
        _node2=node2;
        _color=color;

        DistanceJointDef distJointDef = new DistanceJointDef();
        Vector2 p1=_node1.body.getPosition();
        Vector2 p2=_node2.body.getPosition();
        distJointDef.initialize(_node1.body, _node2.body, p1, p2);
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
        revoluteJointDef1.initialize(node1.body, _body1, node1.body.getPosition());
        revoluteJointDef1.collideConnected=false;
        _revoluteJoint1=(RevoluteJoint)world.createJoint(revoluteJointDef1);

        RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();
        revoluteJointDef2.initialize(node2.body, _body2, node2.body.getPosition());
        revoluteJointDef2.collideConnected=false;
        _revoluteJoint2=(RevoluteJoint)world.createJoint(revoluteJointDef2);

        PrismaticJointDef prismaticJointDef=new PrismaticJointDef();
        prismaticJointDef.initialize(_body1,_body2,center,axis);
        _prismaticJoint=(PrismaticJoint)world.createJoint(prismaticJointDef);


        update();
    }

    public void update(){
        Vector2 p1 = node1().body.getPosition();
        Vector2 p2 = node2().body.getPosition();
        float dist = p1.dst(p2);
        if(Math.abs((dist-_lastUpdateDist)/_lastUpdateDist)<.1){
            return;
        }
        if(_fixture1!=null){
            _body1.destroyFixture(_fixture1);
            _body2.destroyFixture(_fixture2);
        }
        float w=dist*.75f;
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

        _lastUpdateDist = dist;
    }
    public void setLength(float length){
        _distanceJoint.setLength(length);
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
    private float _lastUpdateDist;
}
