package com.gmail.wpalfi.mech;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class Node implements Drawable{
    private World _world;
    private OrthographicCamera _camera;
    private Body _body;

    public Node(OrthographicCamera camera, World world, Vector2 startPos) {
        _world=world;
        _camera=camera;
        BodyDef bd = new BodyDef();
        bd.allowSleep = false;
        bd.position.set(startPos.x, startPos.y);
        _body = world.createBody(bd);
        _body.setType(BodyDef.BodyType.DynamicBody);
    }

    @Override
    public float hitTest(Vector2 pos) {
        Vector2 center = _body.getPosition();
        float dist = pos.dst(center)-radius();
        dist=Math.max(0,dist);
        return dist;
    }

    private float radius(){
        return .5f * worldMeterPerScreenCm();
    }

    public void render(ShapeRenderer renderer, boolean hover) {
        renderer.begin(hover ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
        renderer.setColor(.5f,.5f,.5f,1);
        Vector2 pos = _body.getPosition();
        renderer.circle(pos.x,pos.y,radius(),64);
        renderer.end();
    }

    private float pixPerWorldMeter() {
        return Gdx.graphics.getWidth() / _camera.viewportWidth;
    }
    private float worldMeterPerScreenCm() {
        float pixPerScreenCm = Gdx.graphics.getPpcX();
        return pixPerScreenCm / pixPerWorldMeter();
    }

    public void destroy(){
        _world.destroyBody(_body);
    }

    public Body getBody(){
        return _body;
    }
}

