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

        nodes.add(new Node(cam, world, 2,6,(float)0.3,Color.WHITE));
        nodes.add(new Node(cam, world, 9,5,(float)0.3,Color.WHITE));
        nodes.add(new Node(cam, world, 6,4,(float)0.3,Color.YELLOW));

        edges.add(new Edge(cam, world, nodes.get(0),nodes.get(1),Color.WHITE));
        edges.add(new Edge(cam, world, nodes.get(0),nodes.get(2),Color.YELLOW));
        edges.add(new Edge(cam, world, nodes.get(1),nodes.get(2),Color.YELLOW));

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
        //if(drags.isEmpty()){
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
        //}
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
        grid();
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

    private void grid(){
        int x0=(int)Math.floor(cam.position.x-cam.viewportWidth/2);
        int x1=(int)Math.ceil(cam.position.x+cam.viewportWidth/2);
        int y0=(int)Math.floor(cam.position.y-cam.viewportHeight/2);
        int y1=(int)Math.ceil(cam.position.y+cam.viewportHeight/2);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(.5f,.5f,.5f,0.25f);
        for(int x=x0;x<=x1;x++) {
            renderer.line(x,y0,x,y1);
        }
        for(int y=y0;y<=y1;y++) {
            renderer.line(x0,y,x1,y);
        }
        renderer.end();
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
        float maxDist = .7f*this.worldMeterPerScreenCm();
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
                hitTestResult.dist=dist;
                hitTestResult.drawable=drawable;
            }else if(dist<hitTestResult.dist) {
                hitTestResult.dist=dist;
                hitTestResult.drawable=drawable;
            }
        }
        return hitTestResult;
    }



    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        finishDragOrZoom();
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
        drag.screenPix=drag.startScreenPix;
        drag.startCamPosition = new Vector3(cam.position);
        drag.startViewportWidth = cam.viewportWidth;
        drag.start = new Vector2(mousePos);
        drag.startDrawable=hitTest(mousePos);
        if(drag.startDrawable instanceof Slide) {
            drag.type=DragType.SLIDE;
            Slide slide=(Slide)drag.startDrawable;
            slide.touchDown(mousePos);
            _selection.clear();
            onSelectionChanged();
        }
        if(_properties.tool==Tool.EDGE
                && drag.startDrawable instanceof Node){
            drag.type=DragType.DRAWEDGE;
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
        drag.screenPix=new Vector2(screenX,screenY);
        drag.endDrawable=hitTest(mousePos);
        dragEnd(drag);
        drags.remove(pointer);
        return true;
    }
    private void finishDragOrZoom(){
        for(Drag drag:drags.values()) {
            if (drag.type==DragType.PAN || drag.type==DragType.ZOOM) {
                drag.startDrawable = null;
                drag.startScreenPix = new Vector2(drag.screenPix);
                drag.startCamPosition = new Vector3(cam.position);
                drag.startViewportWidth = cam.viewportWidth;
            }
        }
    }
    private void dragEnd(Drag drag) {
        if(drag.type==DragType.PAN||drag.type==DragType.ZOOM){
            finishDragOrZoom();
        }
        if(drag.type==DragType.SLIDE){
            Slide slide = (Slide)drag.startDrawable;
            slide.touchUp(drag.end);
            return;
        }
        if(drag.type==DragType.DRAWEDGE
                && drag.endDrawable instanceof Node) {
            Node start =(Node)drag.startDrawable;
            Node end=(Node)drag.endDrawable;
            edges.add(new Edge(cam, world, start, end, _properties.color));
            return;
        }
        //tap
        if(drag.type==DragType.UNDEFINED) {
            if (draw(drag)) {
                return;
            }
            select(drag);
        }
    }
    private float pixPerWorldMeter() {
        return Gdx.graphics.getWidth() / cam.viewportWidth;
    }
    private float worldMeterPerScreenCm() {
        float pixPerScreenCm = Gdx.graphics.getPpcX();
        return pixPerScreenCm / pixPerWorldMeter();
    }

    private void select(Drag drag){
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

    private boolean draw(Drag drag){
        if(drag.startDrawable!=null){
            return false;
        }
        float maxDist = .2f * worldMeterPerScreenCm();
        float dist = drag.end.dst(drag.start);
        if(dist>maxDist){
            return false;
        }
        if(_properties.tool==Tool.NODE){
            Node node=new Node(cam, world, drag.end.x,drag.end.y,_properties.radius,_properties.color);
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
            drag.screenPix=new Vector2(screenX,screenY);
            float minMovePix = 0.1f * Gdx.graphics.getPpcX();
            float distPix = drag.screenPix.dst(drag.startScreenPix);
            if(drag.type==DragType.UNDEFINED && distPix>=minMovePix) {
                if(isZoomGesture(drag))
                    drag.type=DragType.ZOOM;
                else
                    drag.type=DragType.PAN;
            }
            if(drag.type == DragType.SLIDE){
                Slide slide = (Slide)drag.startDrawable;
                slide.touchDragged(mousePos);
                return true;
            }
            if(pan(drag)){
                return true;
            }
            if(zoom(drag)){
                return true;
            }
            return true;
        }
        return true;
    }

    private boolean pan(Drag drag){
        if(!isPanGesture(drag)) {
            return false;
        }
        if(drag.type!=DragType.PAN){
            //new pan
            finishDragOrZoom();
            drag.type=DragType.PAN;
        }
        Vector2 moveWorld = worldMove(drag);
        cam.position.x = drag.startCamPosition.x - moveWorld.x;
        cam.position.y = drag.startCamPosition.y + moveWorld.y;
        cam.update();
        drag.type=DragType.PAN;
        return true;
    }
    private boolean isZoomGesture(Drag drag){
        if (!(drag.type==DragType.PAN || drag.type==DragType.ZOOM)) {
            return false;
        }
        return countPanOrZoomDrags()==2;
    }
    private boolean isPanGesture(Drag drag){
        if (!(drag.type==DragType.PAN || drag.type==DragType.ZOOM)) {
            return false;
        }
        return countPanOrZoomDrags()==1;
    }
    private int countPanOrZoomDrags(){
        int count=0;
        for(Drag d:drags.values()){
            if(d.type==DragType.PAN || d.type==DragType.ZOOM){
                count++;
            }
        }
        return count;
    }
    private boolean zoom(Drag drag) {
        if (!isZoomGesture(drag)) {
            return false;
        }
        Drag drag1=null,drag2=null;
        for(Drag d:drags.values()){
            if(d.type==DragType.PAN || d.type==DragType.ZOOM){
                if(drag1==null)drag1=d;else drag2=d;
            }
        }
        if(drag1.type!=DragType.ZOOM || drag2.type!=DragType.ZOOM){
            //new zoom
            finishDragOrZoom();
            drag1.type=DragType.ZOOM;
            drag2.type=DragType.ZOOM;
        }
        Vector2 moveWorld1 = worldMove(drag1);
        Vector2 moveWorld2 = worldMove(drag2);
        cam.position.x = drag.startCamPosition.x - (moveWorld1.x + moveWorld2.x)/2;
        cam.position.y = drag.startCamPosition.y + (moveWorld1.y + moveWorld2.y)/2;
        float zoom = drag1.screenPix.dst(drag2.screenPix) / drag1.startScreenPix.dst(drag2.startScreenPix);
        cam.viewportWidth = drag.startViewportWidth / zoom;
        cam.viewportHeight = cam.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        cam.update();
        return true;
    }
    private Vector2 worldMove(Drag drag){
        Vector2 movePix = new Vector2(drag.screenPix).sub(drag.startScreenPix);
        Vector2 moveWorld = new Vector2(movePix).scl(1/pixPerWorldMeter());
        return moveWorld;
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
