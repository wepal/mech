package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Node implements Drawable{
    private Camera _camera;
    private float _x,_y;
    private float _weight = 1;
    private float _radius = 1;
    private Color _color;
    private Body _body;
    private Fixture _fixture;

    public Node(Camera camera, World world, float x_, float y_, float radius_, Color color_) {
        _camera=camera;
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

    @Override
    public float hitTest(Vector2 pos) {
        Vector2 center = _body.getPosition();
        float dist = pos.dst(center)-_radius;
        dist = Math.max(dist,0f);
        return dist;
    }

    public void render(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Line);
        Util.setRendererColor(renderer, _color);
        Vector2 pos = _body.getPosition();
        renderer.circle(pos.x,pos.y,_radius,64);
        renderer.end();
    }

    public void renderSelection(ShapeRenderer renderer) {
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(.3f,.3f,.3f,1);
        Vector2 pos = _body.getPosition();
        float margin =  .3f * worldMeterPerScreenCm();
        renderer.circle(pos.x,pos.y,_radius+margin,64);
        renderer.end();
    }
    private float pixPerWorldMeter() {
        return Gdx.graphics.getWidth() / _camera.viewportWidth;
    }
    private float worldMeterPerScreenCm() {
        float pixPerScreenCm = Gdx.graphics.getPpcX();
        return pixPerScreenCm / pixPerWorldMeter();
    }

}

