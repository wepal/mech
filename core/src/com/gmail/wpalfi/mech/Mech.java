package com.gmail.wpalfi.mech;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
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
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Mech extends ApplicationAdapter implements InputProcessor, MenuConsumer, Slider.Listener {
    List<Node> nodes = new ArrayList<Node>();;
    List<Edge> edges = new ArrayList<Edge>();
    List<Slide> slides = new ArrayList<Slide>();
    World world, pullWorld;
    OrthographicCamera cam;
    ShapeRenderer renderer;
    HashMap<Integer,Drag> drags=new HashMap<Integer, Drag>();
    Box2DDebugRenderer _debugRenderer;
    Properties _properties=new Properties();
    List<Object> _selection=new ArrayList<Object>();
    boolean _selectMulti=false;
    ToolBar _toolBar;
    Slider _dragSlider;

    @Override
    public Properties getProperties(){
        return new Properties(_properties);
    }

    @Override
    public void setTool(Tool tool){
        _properties.tool=tool;
    }

    @Override
    public void setColor(Color color){
        _properties.color = color;
        for (Object o : _selection) {
            if(o instanceof Node){
                Node node=(Node)o;
                node.setColor(color);
            }
            if(o instanceof Edge){
                Edge edge=(Edge)o;
                edge.setColor(color);
            }
        }
    }

    @Override
    public void setRadius(float radius){
        _properties.radius=radius;
        for (Object o : _selection) {
            if(o instanceof Node){
                Node node=(Node)o;
                node.setRadius(radius);
            }
        }
    }

    @Override
    public void setPause(boolean pause) {
        _properties.pause=pause;
    }

    @Override
	public void create () {
        renderer= new ShapeRenderer();
        _debugRenderer = new Box2DDebugRenderer();
        Vector2 gravity = new Vector2(0.0f, -10.0f);
        boolean doSleep = true;
        world = new World(gravity, doSleep);
        //world.setContactListener(this);
        pullWorld=new World(new Vector2(0,0),doSleep);
        cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(this);

        nodes.add(new Node(world, 2,6,(float)0.3,Color.WHITE));
        nodes.add(new Node(world, 9,5,(float)0.3,Color.WHITE));
        nodes.add(new Node(world, 6,4,(float)0.3,Color.YELLOW));

        edges.add(new Edge(world, nodes.get(0),nodes.get(1),Color.WHITE));
        edges.add(new Edge(world, nodes.get(0),nodes.get(2),Color.YELLOW));
        edges.add(new Edge(world, nodes.get(1),nodes.get(2),Color.YELLOW));

        Slide slide = new Slide(pullWorld,new Vector2(10,5),new Vector2(12,7),.33f);
        slides.add(slide);

        Drive drive=new Drive();
        drive.slide=slide;
        drive.length=1.5f;
        edges.get(1).addDrive(drive);

        _toolBar=new ToolBar(this);

        cam.viewportWidth = 20;
        cam.viewportHeight = cam.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        cam.position.set(cam.viewportWidth / 2, cam.viewportHeight / 2, 0);
        cam.update();
    }


    private void tick (float timeStep, int iters) {
        for(int i=0; i<edges.size();i++) {
            edges.get(i).update();
        }

        float dt = timeStep / iters;
        for (int i = 0; i < iters; i++) {
            world.step(dt, 10, 10);
            pullWorld.step(dt,10,10);
        }
    }

	@Override
	public void render () {
        GL20 gl = Gdx.gl;

        long startPhysics = TimeUtils.nanoTime();
        if (!_properties.pause) {
            tick(Gdx.graphics.getDeltaTime(), 4);
        }

        //gl.glClearColor(108 / (float) 255, 96 / (float) 255, 15 / (float) 255, 1);
        gl.glClearColor(0,0,0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(_properties.useDebugRenderer){
            _debugRenderer.render(world,cam.combined);
            _debugRenderer.render(pullWorld,cam.combined);

        }else{
            renderNormal();
        }

        _toolBar.render();
        if(_dragSlider!=null){
            _dragSlider.render();
        }
    }

    private void renderNormal(){
        renderer.setProjectionMatrix(cam.combined);
        if(drags.isEmpty()){
            for (Object o : _selection) {
                if(o instanceof Node){
                    Node node=(Node)o;
                    node.renderSelection(renderer);
                }
                if(o instanceof Edge){
                    Edge edge=(Edge)o;
                    edge.renderSelection(renderer);
                }
            }
        }
        for (Edge edge : edges) {
            boolean hasDrive =false;
            for (Drag drag: drags.values()) {
                if(drag.startDrawable instanceof Slide){
                    Slide slide=(Slide)drag.startDrawable;
                    if(edge.hasDrive(slide)){
                        hasDrive=true;
                    }
                }
            }
            if(hasDrive){
                edge.renderDrive(renderer, _selection.contains(edge));
            }
        }
        for (Edge edge : edges) {
            edge.render(renderer);
        }
        for (Node node : nodes) {
            node.render(renderer);
        }
        for (Slide slide : slides) {
            slide.render(renderer);
        }
    }
	
	@Override
	public void dispose () {
        //TODO
	}

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    private Drawable hitTest(Vector2 mousePos) {
        HitTestResult hitTestResult = hitTest(null, edges, mousePos);
        hitTestResult = hitTest(hitTestResult ,nodes, mousePos);
        hitTestResult = hitTest(hitTestResult ,slides, mousePos);
        if(hitTestResult==null) {
            return null;
        }
        float maxDist = screenCmToWorldDist(.2f);
        if(hitTestResult.dist>maxDist) {
            return null;
        }
        return hitTestResult.drawable;
    }

    private class HitTestResult {
        Drawable drawable;
        float dist;
    }

    private HitTestResult hitTest(HitTestResult hitTestResult ,List<? extends Drawable> drawables, Vector2 mousePos) {
        for (Drawable drawable : drawables) {
            float dist = drawable.hitTest(mousePos);
            if(hitTestResult==null) {
                hitTestResult = new HitTestResult();
            }else if(dist>hitTestResult.dist) {
                continue;
            }
            hitTestResult.dist=dist;
            hitTestResult.drawable=drawable;
        }
        return hitTestResult;
    }



    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (_toolBar.touchDown(screenX, screenY, pointer, button)) {
            return true;
        }
        if (_dragSlider != null) {
            if (_dragSlider.touchDown(screenX, screenY, pointer, button)) {
                return true;
            }
            _dragSlider = null;
        }
        Vector3 mousePos3 = cam.unproject(new Vector3(screenX, screenY, 0));
        Vector2 mousePos = new Vector2(mousePos3.x, mousePos3.y);
        Drag drag = new Drag();
        drag.startScreenPix = new Vector2(screenX,screenY);
        drag.startCamPosition = new Vector3(cam.position);
        drag.start = mousePos;
        drag.startDrawable=hitTest(mousePos);
        if(drag.startDrawable instanceof Slide) {
            Slide slide=(Slide)drag.startDrawable;
            slide.touchDown(mousePos);
            _selection.clear();
            onSelectionChanged();
        }
        drags.put(pointer, drag);
        return true;
    }
    private void onSelectionChanged(){
        for (Object o : _selection) {
            if(o instanceof Node){
                _properties.radius=((Node)o).getRadius();
            }
        }
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(_toolBar.touchUp(screenX,screenY, pointer, button)){
            return true;
        }
        if(_dragSlider!=null){
            if(_dragSlider.touchUp(screenX,screenY,pointer,button)){
                return true;
            }
        }
        Vector3 mousePos3 = cam.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        if(!drags.containsKey(pointer)) {
            return true;
        }
        Drag drag=drags.get(pointer);
        drag.end=mousePos;
        drag.endDrawable=hitTest(mousePos);
        dragEnd(drag);
        drags.remove(pointer);
        return true;
    }

    private void dragEnd(Drag drag) {
        if(drag.startDrawable instanceof Slide){
            Slide slide = (Slide)drag.startDrawable;
            slide.touchUp(drag.end);
            return;
        }
        if(_properties.tool==Tool.EDGE
                && drag.startDrawable instanceof Node
                && drag.endDrawable instanceof Node) {
            Node start =(Node)drag.startDrawable;
            Node end=(Node)drag.endDrawable;
            edges.add(new Edge(world, start, end, _properties.color));
            return;
        }
        //tap
        if(draw(drag)){
            return;
        }
        if(drag.startDrawable==null) {
            _selection.clear();
            onSelectionChanged();
            return;
        }
        if(!_selectMulti) {
            _selection.clear();
        }
        _selection.add(drag.startDrawable);
        onSelectionChanged();

        if(drag.startDrawable instanceof  Edge && drags.size()==2){
            Edge edge=(Edge)drag.startDrawable;
            Slide slide=null;
            Drag drag1 = (Drag)drags.values().toArray()[0];
            Drag drag2 = (Drag)drags.values().toArray()[1];
            if(drag1.startDrawable instanceof Slide)
                slide=(Slide)drag1.startDrawable;
            if(drag2.startDrawable instanceof Slide)
                slide=(Slide)drag2.startDrawable;
            if(slide!=null) {
                Drive drive = edge.getDrive(slide);
                if (drive == null) {
                    drive = new Drive();
                    drive.slide = slide;
                    drive.length = 1;
                    edge.addDrive(drive);
                }
                if (_dragSlider == null) {
                    _dragSlider = new Slider(drive.length, this);
                }
            }
        }
    }
    private float screenCmToWorldDist(float screenDistCm){
        float pixPerWorldMeter = Gdx.graphics.getWidth() / cam.viewportWidth;
        float pixPerScreenCm = Gdx.graphics.getPpcX();
        return screenDistCm * pixPerScreenCm / pixPerWorldMeter;
    }
    private boolean draw(Drag drag){
        if(drag.startDrawable!=null){
            return false;
        }
        float maxDist = screenCmToWorldDist(.2f);
        float dist = drag.end.dst(drag.start);
        if(dist>maxDist){
            return false;
        }
        if(_properties.tool==Tool.NODE){
            Node node=new Node(world, drag.end.x,drag.end.y,_properties.radius,_properties.color);
            nodes.add(node);
            return true;
        }
        if(_properties.tool==Tool.SLIDE){
            Slide slide = new Slide(pullWorld, new Vector2(drag.end).add(0,1), new Vector2(drag.end).add(0,-1),.5f);
            slides.add(slide);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 mousePos3 = cam.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        if(_toolBar.touchDragged(screenX,screenY, pointer)){
            return true;
        }
        if(_dragSlider!=null){
            if(_dragSlider.touchDragged(screenX,screenY,pointer)){
                return true;
            }
        }
        if(drags.containsKey(pointer)){
            Drag drag = drags.get(pointer);
            if(drag.startDrawable instanceof Slide){
                Slide slide = (Slide)drag.startDrawable;
                slide.touchDragged(mousePos);
                return true;
            }
            if(drag.startDrawable==null && drags.size()==1){
                Vector2 movePix = new Vector2(screenX,screenY).sub(drag.startScreenPix);
                float pixPerWorldMeter = Gdx.graphics.getWidth() / cam.viewportWidth;
                Vector2 moveWorld = new Vector2(movePix).scl(1/pixPerWorldMeter);
                cam.position.x = drag.startCamPosition.x - moveWorld.x;
                cam.position.y = drag.startCamPosition.y + moveWorld.y;
                cam.update();
            }
        }
        return true;
    }

    private boolean isZoomGesture(){
        if(drags.size()!=2){
            return false;
        }
        for(Drag drag: drags.values()){
            if(drag.startDrawable!=null);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void onSliderValueChanged(float value) {
        for(Drag drag:drags.values()) {
            if(drag.startDrawable instanceof Slide) {
                Slide slide=(Slide)drag.startDrawable;
                for (Object o : _selection) {
                    if(o instanceof Edge){
                        Edge edge=(Edge)o;
                        Drive drive = edge.getDrive(slide);
                        drive.length=value;
                    }
                }
            }
        }
    }
}
