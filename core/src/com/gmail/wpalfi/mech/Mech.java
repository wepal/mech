package com.gmail.wpalfi.mech;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Mech extends ApplicationAdapter implements InputProcessor, MenuConsumer, Slider.Listener {
    List<Node> _nodes = new ArrayList<Node>();;
    List<Edge> _edges = new ArrayList<Edge>();
    List<Slide> _slides = new ArrayList<Slide>();
    World _world, _slideWorld;
    OrthographicCamera _camera, _slideCamera;
    ShapeRenderer _renderer, _slideRenderer;
    HashMap<Integer,Drag> _drags =new HashMap<Integer, Drag>();
    Box2DDebugRenderer _debugRenderer;
    Properties _properties=new Properties();
    List<Object> _selection=new ArrayList<Object>();
    boolean _selectMulti=false;
    ToolBar _toolBar;
    Slider _dragSlider;
    short _lastNodeIndex=0;

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
            if(o instanceof Edge){
                Edge edge=(Edge)o;
                edge.setColor(color);
            }
        }
    }

    @Override
    public void setPause(boolean pause) {
        _properties.pause=pause;
    }

    private short makeNodeIndex(){
        _lastNodeIndex++;
        return _lastNodeIndex;
    }

    @Override
	public void create () {
        _renderer = new ShapeRenderer();
        _debugRenderer = new Box2DDebugRenderer();
        _slideRenderer = new ShapeRenderer();
        Vector2 gravity = new Vector2(0.0f, -10.0f);
        boolean doSleep = true;
        _world = new World(gravity, doSleep);
        //_world.setContactListener(this);
        _slideWorld =new World(new Vector2(0,0),doSleep);

        _camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _camera.viewportWidth = 20;
        _camera.viewportHeight = _camera.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        _camera.position.set(_camera.viewportWidth / 2, _camera.viewportHeight / 2, 0);
        _camera.update();

        _slideCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        _slideCamera.viewportWidth=20;
        _slideCamera.viewportHeight= _slideCamera.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        _slideCamera.position.set(_slideCamera.viewportWidth / 2, _slideCamera.viewportHeight / 2, 0);
        _slideCamera.update();

        Gdx.input.setInputProcessor(this);

        _nodes.add(new Node(_camera, _world, new Vector2(2,6),makeNodeIndex()));
        _nodes.add(new Node(_camera, _world, new Vector2(9,5),makeNodeIndex()));
        _nodes.add(new Node(_camera, _world, new Vector2(6,4),makeNodeIndex()));

        _edges.add(new Edge(_camera, _world, _nodes.get(0), _nodes.get(1),Color.WHITE));
        _edges.add(new Edge(_camera, _world, _nodes.get(0), _nodes.get(2),Color.YELLOW));
        _edges.add(new Edge(_camera, _world, _nodes.get(1), _nodes.get(2),Color.YELLOW));

        Slide slide = new Slide(_slideCamera,_slideWorld,new Vector2(17,6),.33f);
        _slides.add(slide);

        Drive drive=new Drive();
        drive.slide=slide;
        drive.length=1.5f;
        _edges.get(1).addDrive(drive);

        _toolBar=new ToolBar(this);

        /*_world.setContactFilter(new ContactFilter() {
            @Override
            public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
                return false;
            }
        });)*/
    }


    private void tick (float timeStep, int iters) {
        for(int i = 0; i< _edges.size(); i++) {
            _edges.get(i).update();
        }

        float dt = timeStep / iters;
        for (int i = 0; i < iters; i++) {
            _world.step(dt, 10, 10);
            _slideWorld.step(dt,10,10);
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

        //if(!_properties.useDebugRenderer){
            renderNormal();
        //}else{
            _debugRenderer.render(_world, _camera.combined);
            _debugRenderer.render(_slideWorld, _slideCamera.combined);
        //}

        _toolBar.render();
        if(_dragSlider!=null){
            _dragSlider.render();
        }
    }

    private void renderNormal(){
        _renderer.setProjectionMatrix(_camera.combined);
        _slideRenderer.setProjectionMatrix(_slideCamera.combined);
        //if(_drags.isEmpty()){
            /*for (Object o : _selection) {
                if(o instanceof Node){
                    Node node=(Node)o;
                    node.render(_renderer);
                }
            }*/
        //}
        grid();
        for (Edge edge : _edges) {
            edge.render(_renderer);
        }
        /*for(Drag drag: _drags.values()){
            if(drag.type==DragType.DRAWEDGE){
                Edge.renderFloatingEdge(_renderer,_camera,(Node)drag.startDrawable,drag.currentPix,_properties.color);
            }
        }*/
        for (Node node : _nodes) {
            boolean hover=false;
            for(Drag drag :_drags.values()){
                if(drag.startDrawable == node || drag.currentDrawable==node)
                    hover=true;
            }
            node.render(_renderer, hover);
        }
        for (Slide slide : _slides) {
            slide.render(_slideRenderer);
        }
    }

    private void grid(){
        int x0=(int)Math.floor(_camera.position.x- _camera.viewportWidth/2);
        int x1=(int)Math.ceil(_camera.position.x+ _camera.viewportWidth/2);
        int y0=(int)Math.floor(_camera.position.y- _camera.viewportHeight/2);
        int y1=(int)Math.ceil(_camera.position.y+ _camera.viewportHeight/2);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        _renderer.begin(ShapeRenderer.ShapeType.Line);
        _renderer.setColor(.5f,.5f,.5f,0.25f);
        for(int x=x0;x<=x1;x++) {
            _renderer.line(x,y0,x,y1);
        }
        for(int y=y0;y<=y1;y++) {
            _renderer.line(x0,y,x1,y);
        }
        _renderer.end();
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

    private Drawable hitTest(Vector2 mousePosPix) {
        float maxDistPix = .7f * Gdx.graphics.getPpcX();
        HitTestResult hitTestResult = hitTest(null, _slides, mousePosPix, _slideCamera);
        hitTestResult = hitTest(hitTestResult , _nodes, mousePosPix, _camera);
        if(hitTestResult!=null && hitTestResult.distPix <= maxDistPix) {
            return hitTestResult.drawable;
        }
        hitTestResult = hitTest(null, _edges, mousePosPix, _camera);
        if(hitTestResult!=null && hitTestResult.distPix <= maxDistPix) {
            return hitTestResult.drawable;
        }
        return null;
    }

    private class HitTestResult {
        Drawable drawable;
        float distPix;
    }

    private HitTestResult hitTest(HitTestResult hitTestResult, List<? extends Drawable> drawables, Vector2 mousePosPix, OrthographicCamera camera) {
        Vector3 mousePosPix3 = new Vector3(mousePosPix.x,mousePosPix.y,0);
        Vector3 mousePos3 = camera.unproject(mousePosPix3);
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        for (Drawable drawable : drawables) {
            float dist = drawable.hitTest(mousePos);
            float distPix = dist * Gdx.graphics.getWidth() / camera.viewportWidth;
            if(hitTestResult==null) {
                hitTestResult = new HitTestResult();
                hitTestResult.distPix=distPix;
                hitTestResult.drawable=drawable;
            }else if(distPix<hitTestResult.distPix) {
                hitTestResult.distPix=distPix;
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
        Vector3 mousePos3 = _camera.unproject(new Vector3(screenX, screenY, 0));
        Vector2 mousePos = new Vector2(mousePos3.x, mousePos3.y);
        Drag drag = new Drag();
        drag.startPix = new Vector2(screenX,screenY);
        drag.currentPix = new Vector2(drag.startPix);
        drag.startCamPosition = new Vector3(_camera.position);
        drag.startViewportWidth = _camera.viewportWidth;
        drag.startDrawable=hitTest(drag.startPix);
        if(drag.startDrawable instanceof Slide
                && _properties.tool!=Tool.DELETE) {
            drag.type=DragType.SLIDE;
            Slide slide=(Slide)drag.startDrawable;
            if(!slide.isDragging()) {
                slide.touchDown(drag.currentPix);
                _selection.clear();
                onSelectionChanged();
            }
        }
        if(_properties.tool==Tool.EDGE){
            drag.type=DragType.DRAWEDGE;
        }
        _drags.put(pointer, drag);
        return true;
    }
    private void onSelectionChanged(){
        /*for (Object o : _selection) {
            if(o instanceof Node){
                _properties.radius=((Node)o).getRadius();
            }
        }*/
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
        Vector3 mousePos3 = _camera.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        if(!_drags.containsKey(pointer)) {
            return true;
        }
        Drag drag= _drags.get(pointer);
        drag.endPix=new Vector2(drag.currentPix);
        drag.endDrawable=hitTest(drag.endPix);
        dragEnd(drag);
        _drags.remove(pointer);
        return true;
    }
    private void finishDragOrZoom(){
        for(Drag drag: _drags.values()) {
            if (drag.type==DragType.PAN || drag.type==DragType.ZOOM) {
                drag.startDrawable = null;
                drag.startPix = new Vector2(drag.currentPix);
                drag.startCamPosition = new Vector3(_camera.position);
                drag.startViewportWidth = _camera.viewportWidth;
            }
        }
    }
    private void dragEnd(Drag drag) {
        if(drag.type==DragType.PAN||drag.type==DragType.ZOOM){
            finishDragOrZoom();
            return;
        }
        if(drag.type==DragType.SLIDE){
            Slide slide = (Slide)drag.startDrawable;
            slide.touchUp(drag.endPix);
            return;
        }
        if(drag.type==DragType.DRAWEDGE) {
            boolean hasStartNode = drag.startDrawable instanceof Node;
            boolean hasEndNode = drag.endDrawable instanceof Node;
            if(!(hasStartNode && hasEndNode && drag.startDrawable==drag.endDrawable)){
                Node node1 = hasStartNode ? (Node)drag.startDrawable : makeNode(drag.startPix);
                Node node2 = hasEndNode ? (Node)drag.endDrawable: makeNode(drag.endPix);
                _edges.add(new Edge(_camera, _world, node1, node2, _properties.color));
                return;
            }
        }
        //tap
        if(drag.type==DragType.UNDEFINED) {
            if(delete(drag)){
                return;
            }
            if (draw(drag)) {
                return;
            }
            select(drag);
        }
    }
    private Node makeNode(Vector2 pix){
        Vector2 pos = Util.unproject(_camera,pix);
        Node node = new Node(_camera,_world,pos,makeNodeIndex());
        _nodes.add(node);
        return node;
    }
    private float pixPerWorldMeter() {
        return Gdx.graphics.getWidth() / _camera.viewportWidth;
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

        if(drag.startDrawable instanceof  Edge && _drags.size()==2){
            Edge edge=(Edge)drag.startDrawable;
            Slide slide=null;
            Drag drag1 = (Drag) _drags.values().toArray()[0];
            Drag drag2 = (Drag) _drags.values().toArray()[1];
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
                    _dragSlider = new Slider(drive.length-1, this);
                }
            }
        }
    }

    private boolean draw(Drag drag){
        if(drag.startDrawable!=null){
            return false;
        }
        float maxDistPix = .2f / Gdx.graphics.getPpcX();
        float distPix = drag.endPix.dst(drag.startPix);
        if(distPix>maxDistPix){
            return false;
        }
        if(_properties.tool==Tool.SLIDE){
            Vector2 pos = Util.unproject(_slideCamera, drag.startPix);
            Slide slide = new Slide(_slideCamera, _slideWorld, pos,.5f);
            _slides.add(slide);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 mousePos3 = _camera.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        if(_toolBar.touchDragged(screenX,screenY, pointer)){
            return true;
        }
        if(_dragSlider!=null){
            if(_dragSlider.touchDragged(screenX,screenY,pointer)){
                return true;
            }
        }
        if(_drags.containsKey(pointer)){
            Drag drag = _drags.get(pointer);
            drag.currentPix=new Vector2(screenX,screenY);
            drag.currentDrawable=hitTest(drag.currentPix);
            float minMovePix = 0.1f * Gdx.graphics.getPpcX();
            float distPix = drag.currentPix.dst(drag.startPix);
            if(drag.type==DragType.UNDEFINED && distPix>=minMovePix) {
                for(Drag d: _drags.values()){
                    if(d.type==DragType.UNDEFINED)
                        d.type=DragType.ZOOM;
                }
                if(isZoomGesture(drag))
                    drag.type=DragType.ZOOM;
                else
                    drag.type=DragType.PAN;
            }
            if(drag.type == DragType.SLIDE){
                Slide slide = (Slide)drag.startDrawable;
                slide.touchDragged(drag.currentPix);
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
        _camera.position.x = drag.startCamPosition.x - moveWorld.x;
        _camera.position.y = drag.startCamPosition.y + moveWorld.y;
        _camera.update();
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
        for(Drag d: _drags.values()){
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
        for(Drag d: _drags.values()){
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
        _camera.position.x = drag.startCamPosition.x - (moveWorld1.x + moveWorld2.x)/2;
        _camera.position.y = drag.startCamPosition.y + (moveWorld1.y + moveWorld2.y)/2;
        float zoom = drag1.currentPix.dst(drag2.currentPix) / drag1.startPix.dst(drag2.startPix);
        _camera.viewportWidth = drag.startViewportWidth / zoom;
        _camera.viewportHeight = _camera.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        _camera.update();
        return true;
    }
    private Vector2 worldMove(Drag drag){
        Vector2 movePix = new Vector2(drag.currentPix).sub(drag.startPix);
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
        for(Drag drag: _drags.values()) {
            if(drag.startDrawable instanceof Slide) {
                Slide slide=(Slide)drag.startDrawable;
                for (Object o : _selection) {
                    if(o instanceof Edge){
                        Edge edge=(Edge)o;
                        Drive drive = edge.getDrive(slide);
                        drive.length = 1 + value;
                    }
                }
            }
        }
    }

    private boolean delete(Drag drag){
        if(_properties.tool!=Tool.DELETE){
            return false;
        }
        if(drag.startDrawable!=null){
            delete(drag.startDrawable);
        }
        return true;
    }
    private void delete(Drawable drawable){
        if(drawable instanceof Node){
            Node node=(Node)drawable;
            _nodes.remove(node);
            for(Edge edge:new ArrayList<Edge>(_edges)){
                if(edge.node1()==node || edge.node2()==node){
                    delete(edge);
                }
            }
            node.destroy();
        }
        if(drawable instanceof Edge){
            Edge edge=(Edge)drawable;
            edge.destroy();
            _edges.remove(edge);
        }
        if(drawable instanceof Slide){
            Slide slide=(Slide)drawable;
            slide.destroy();
            _slides.remove(slide);
            for(Edge edge:new ArrayList<Edge>(_edges)){
                edge.removeDrives(slide);
            }
        }
    }
}
