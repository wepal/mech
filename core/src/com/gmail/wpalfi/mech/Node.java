package com.gmail.wpalfi.mech;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Node {
    private float _x,_y;
    private float _weight = 1;
    private float _radius = 1;
    private Color _color;
    private Body _body;
    private Fixture _fixture;

    public Node(World world, float x_, float y_, float radius_, Color color_) {
        _x=x_;
        _y=y_;
        _radius=radius_;
        _color=color_;


        BodyDef bd = new BodyDef();
        // bd.isBullet = true;
        bd.allowSleep = true;
        bd.position.set(_x, _y);
        //bd.fixedRotation = true;
        _body = world.createBody(bd);
        _body.setType(BodyDef.BodyType.DynamicBody);
        updateFixture();
    }

    public void setColor(Color color){
        _color=color;
        updateFixture();
    }

    public void setRadius(float radius) {
        _radius = radius;
        updateFixture();
    }

    private void updateFixture(){
        if(_fixture!=null){
            _body.destroyFixture(_fixture);
            _fixture=null;
        }
        CircleShape sd = new CircleShape();
        sd.setRadius(_radius);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 1.0f;
        fdef.friction = 0.3f;
        fdef.restitution = 0.6f;

        _fixture = _body.createFixture(fdef);
        if(_color==Color.WHITE)
            _body.setType(BodyDef.BodyType.StaticBody);
        else
            _body.setType(BodyDef.BodyType.DynamicBody);
    }
    public Body getBody(){
        return _body;
    }
    float getRadius(){
        return _radius;
    }
    Color getColor(){
        return _color;
    }

    public void render(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Line);
        ColorUtil.setRendererColor(renderer, _color);
        Vector2 pos = _body.getPosition();
        renderer.circle(pos.x,pos.y,_radius,64);
        renderer.end();
    }

    public void renderSelection(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(.2f,.2f,.2f,1);
        Vector2 pos = _body.getPosition();
        renderer.circle(pos.x,pos.y,_radius+.6f,64);
        renderer.end();
    }
}

