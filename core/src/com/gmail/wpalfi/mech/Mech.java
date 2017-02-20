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
import com.badlogic.gdx.utils.TimeUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Mech extends ApplicationAdapter implements InputProcessor, MenuConsumer {
    List<Node> nodes = new ArrayList<Node>();;
    List<Edge> edges = new ArrayList<Edge>();
    List<Slide> slides = new ArrayList<Slide>();
    World world, pullWorld;
    OrthographicCamera cam;
    ShapeRenderer renderer;
    HashMap<Integer,Drag> drags=new HashMap<Integer, Drag>();
    Node _drawEdgeNode;
    Box2DDebugRenderer _debugRenderer;
    Properties _properties=new Properties();
    List<Object> _selection=new ArrayList<Object>();
    ToolBar _toolBar;

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

        Slide slide =createSlide(9,3);
        Drive drive=new Drive();
        drive.edge=1;
        drive.length=9;
        slide.drives.add(drive);
        slides.add(slide);

        slide =createSlide(9,6);
        drive=new Drive();
        drive.edge=2;
        drive.length=9;
        slide.drives.add(drive);
        slides.add(slide);

        _toolBar=new ToolBar(this);
    }

    private Slide createSlide(float x, float y) {
        Slide slide =new Slide();
        slide.homeX=x;
        slide.homeY=y;

        CircleShape sd = new CircleShape();
        sd.setRadius(1);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = sd;
        fdef.density = 1.0f;
        fdef.friction = 0.3f;
        fdef.restitution = 0.6f;

        BodyDef bd = new BodyDef();
        // bd.isBullet = true;
        bd.linearDamping = 1f;
        bd.fixedRotation = true;
        bd.allowSleep = true;
        bd.position.set(x, y);
        Body body = pullWorld.createBody(bd);
        body.createFixture(fdef);
        body.setType(BodyDef.BodyType.DynamicBody);
        slide.body = body;

        BodyDef homeBodyDef = new BodyDef();
        homeBodyDef.allowSleep = true;
        homeBodyDef.position.set(x, y);
        Body homeBody = pullWorld.createBody(homeBodyDef );
        homeBody.setType(BodyDef.BodyType.StaticBody);
        slide.homeBody=homeBody;

        PrismaticJointDef prismaticJointDef=new PrismaticJointDef();
        prismaticJointDef.initialize(homeBody,body,homeBody.getPosition(),new Vector2(0,1));
        slide.prismaticJoint=(PrismaticJoint)pullWorld.createJoint(prismaticJointDef);

        createPullBackJoint(slide);

        return slide;
    }

    private void createPullBackJoint(Slide slide){
        DistanceJointDef pullBackJointDef = new DistanceJointDef();
        pullBackJointDef.dampingRatio=3;
        pullBackJointDef.frequencyHz=3;
        pullBackJointDef.bodyA= slide.body;
        pullBackJointDef.bodyB= slide.homeBody;
        pullBackJointDef.length=0;
        slide.pullBackJoint = (DistanceJoint)pullWorld.createJoint(pullBackJointDef);
    }

    private void tick (float timeStep, int iters) {
        for(int i=0; i<edges.size();i++) {
            edges.get(i).update();
        }
        for(int i = 0; i< slides.size(); i++){
            Slide slide = slides.get(i);
            Vector2 pos = slide.body.getPosition();
            float dist = pos.dst(slide.homeX, slide.homeY);
            for(int j = 0; j< slide.drives.size(); j++) {
                Drive drive = slide.drives.get(j);
                Edge edge = edges.get(drive.edge);
                float weight = Math.min(dist,1f);
                edge.setLength(drive.length * weight + edge.getRestLength() * (1-weight));
            }
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

        gl.glClearColor(108 / (float) 255, 96 / (float) 255, 15 / (float) 255, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.viewportWidth = 20;
        cam.viewportHeight = cam.viewportWidth * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        cam.position.set(cam.viewportWidth / 2, cam.viewportHeight / 2, 0);
        cam.update();

        if(_properties.useDebugRenderer){
            _debugRenderer.render(world,cam.combined);
            _debugRenderer.render(pullWorld,cam.combined);

        }else{
            renderNormal();
        }

        _toolBar.render();
    }

    private void renderNormal(){
        GL20 gl = Gdx.gl;
        renderer.setProjectionMatrix(cam.combined);
        for (Object o : _selection) {
            if(o instanceof Node){
                Node node=(Node)o;
                renderer.begin(ShapeRenderer.ShapeType.Filled);
                renderer.setColor(1,1,1,1);
                Vector2 pos = node.getBody().getPosition();
                renderer.circle(pos.x,pos.y,node.getRadius()+.2f,64);
                renderer.end();
            }
            if(o instanceof Edge){
                Edge edge=(Edge)o;
                edge.renderSelection(renderer);
            }
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            renderer.begin(ShapeRenderer.ShapeType.Line);
            ColorUtil.setRendererColor(renderer, node.getColor());
            Vector2 pos = node.getBody().getPosition();
            gl.glLineWidth(10);
            renderer.circle(pos.x,pos.y,node.getRadius(),64);
            renderer.end();
        }
        for (Edge edge : edges) {
            edge.render(renderer);
        }
        for(int i = 0; i< slides.size(); i++){
            Slide slide = slides.get(i);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(.5f,.5f,1f,.5f);
            Vector2 pos = slide.body.getPosition();
            renderer.circle(pos.x,pos.y,1,64);
            renderer.end();
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

    private Integer findPull(Vector2 mousePos) {
        for(int i = 0; i< slides.size(); i++) {
            Slide slide = slides.get(i);
            Vector2 pos = slide.body.getPosition();
            float dist = pos.dst(mousePos);
            if(dist<1){
                return i;
            }
        }
        return null;
    }

    private Node findNode(Vector2 mousePos) {
        for(int i=0; i<nodes.size();i++) {
            Node node = nodes.get(i);
            Vector2 pos = node.getBody().getPosition();
            float dist = pos.dst(mousePos);
            if(dist<node.getRadius()){
                return node;
            }
        }
        return null;
    }

    private Edge findEdge(Vector2 mousePos){
        for (Edge edge : edges) {
            if(edge.hitTest(mousePos)){
                return edge;
            }
        }
        return null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(_toolBar.touchDown(screenX,screenY, pointer, button)){
            return true;
        }
        Vector3 mousePos3 = cam.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        Edge edge=findEdge(mousePos);
        if(edge!=null){
            if(_selection.contains(edge)){
                _selection.remove(edge);
            }else {
                _selection.add(edge);
            }
            onSelectionChanged();
            return true;
        }
        Integer pull_=findPull(mousePos);
        if(pull_!=null){
            Slide slide = slides.get(pull_);
            MouseJointDef defJoint = new MouseJointDef();
            defJoint.maxForce=10000000* slide.body.getMass();
            //defJoint.dampingRatio=1;
            //defJoint.frequencyHz=1000f;
            defJoint.bodyA= slide.homeBody;
            defJoint.bodyB= slide.body;
            defJoint.target.set(mousePos.x,mousePos.y);
            Drag drag = new Drag();
            drag.mouseJoint = (MouseJoint) pullWorld.createJoint(defJoint);
            drag.pull=pull_;
            drags.put(pointer,drag);
            pullWorld.destroyJoint(slide.pullBackJoint);
            return true;
        }
        if(_properties.tool==Tool.NODE){
            Node node = findNode(mousePos);
            if(node!=null) {
                if(_selection.contains(node)){
                    _selection.remove(node);
                }else {
                    _selection.add(node);
                }
                onSelectionChanged();
            }else{
                node=new Node(world, mousePos.x,mousePos.y,_properties.radius,_properties.color);
                nodes.add(node);
            }
            return true;
        }
        if(_properties.tool==Tool.EDGE){
            Node node = findNode(mousePos);
            if(node!=null){
                _drawEdgeNode=node;
            }
        }
        if(_properties.tool==Tool.SLIDE){
            Slide slide =createSlide(mousePos.x,mousePos.y);
            slides.add(slide);
        }
        return false;
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
        Vector3 mousePos3 = cam.unproject(new Vector3(screenX,screenY,0));
        Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
        if(drags.containsKey(pointer)) {
            Drag drag = drags.get(pointer);
            pullWorld.destroyJoint(drag.mouseJoint);
            drags.remove(pointer);
            createPullBackJoint(slides.get(drag.pull));
        }
        if(_properties.tool==Tool.EDGE && _drawEdgeNode!=null){
            Node node = findNode(mousePos);
            if(node!=null && node!=_drawEdgeNode){
                edges.add(new Edge(world, _drawEdgeNode, node, Color.YELLOW));
                _drawEdgeNode=null;
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(_toolBar.touchDragged(screenX,screenY, pointer)){
            return true;
        }
        if(drags.containsKey(pointer)){
            Vector3 mousePos3 = cam.unproject(new Vector3(screenX,screenY,0));
            Vector2 mousePos = new Vector2(mousePos3.x,mousePos3.y);
            Drag drag = drags.get(pointer);
            drag.mouseJoint.setTarget(mousePos);
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

}
