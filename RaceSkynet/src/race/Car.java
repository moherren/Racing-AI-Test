package race;

import java.util.ArrayList;
import java.util.Arrays;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import Physics.CarBody;
import ai.AI;
import game.Display;
import game.Game;
import geometry.Rectangle;
import geometry.Vector2D;
import graphics.Render;
import graphics.Visible;

public class Car implements Visible, Comparable{
	private CarBody body;
	AI ai;
	final static float feelerLength=100;
	final static double turnPower=0.01,torqueFriction=0.02;
	final static double accelerationPower=0.006,turnLowSpeed=5;
	private float acceleration=0,turn=0;
	int color=0xff000;
	int lap=-1,distance;
	Track track;
	
	
	public Car(float x,float y,double rot,AI ai,Track track){

		body=new CarBody(x,y);
		Vector2D v=new Vector2D(0,4);
		v=v.rotate(rot);
		this.ai=ai;
		this.track=track;
		distance=track.getDistance(getPosition());
	}

	public Vec2 getPosition() {
		return this.body.getBody().getPosition();
	}

	public void update(){
		ArrayList<Double> controls=ai.step(sense());
		float acceleration=0;
		if(controls.get(0)>0.5)
			acceleration=1;
		body.giveInformation(acceleration,controls.get(1).floatValue());
		body.updateBody();
	}
	
	public void render(Render r) {
		body.render(r, color);
	}
	
	public void accelerate(double a){
		acceleration=(float) (a);
	}
	
	public void turn(double torque){
		double speed=body.getSpeed();
		if(speed>turnLowSpeed)
			turn=(float) (torque*turnPower);
		else if(speed!=0)
			turn=(float) (torque*turnPower*speed/turnLowSpeed);
	}

	public double getTorque() {
		return turnPower;
	}
	
	public void destroy(){
		track.getCars().remove(this);
	}
	
	public ArrayList<Double> sense(){
		Double[] output=new Double[16];
		Arrays.fill(output,new Double(0));
		Senses senses=new Senses(body);
		float direction=body.getBody().getAngle();
		
		for(int r=0;r<8;r++){
			float mult=1;
			
			senses.reset();
			Body body=this.body.getBody();
			Display.world.raycast(senses, body.getPosition(), body.getPosition().add(new Vec2((float)Math.cos(direction+Math.PI/4*r)*feelerLength,(float)Math.sin(direction+Math.PI/4*r)*feelerLength)));
			if(senses.getLength()<1){
				
				boolean carSeen=false;
				
				for(Car c:track.getCars())
				{
					if(c.getBody().testPoint(senses.getPoint())){
						output[r+8]=senses.getLength()*mult;
						carSeen=true;
						break;
					}
				}
				if(!carSeen)
				{
					output[r]=senses.getLength()*mult;
				}
			}
		}		
		
		ArrayList<Double> list=new ArrayList<Double>();
		for(Double d:output){
			list.add(d);
		}
		return list;
	}
	
	public CarBody getBody(){
		return body;
	}

	public int compareTo(Object o) {
		Car other=(Car) o;
		return other.getDistance()-getDistance();
	}
	
	public int getDistance() {
		return distance;
	}

	public void applyDistance(int dis){
		if(track.getSegment(dis, 3)==0&&track.getSegment(distance, 3)==2)
			lap++;
		else if(track.getSegment(dis, 3)==2&&track.getSegment(distance, 3)==0)
			lap--;
		distance=(int) (dis+lap*track.length);
	}
	
	public void setColor(int color){
		this.color=color;
	}
}



class Senses implements RayCastCallback{

	Body body;
	Double length=new Double(0);
	Vec2 point=new Vec2(20,20);
	
	public Senses(CarBody body) {
		this.body=body.getBody();
	}

	@Override
	public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
		length= new Double(1-point.sub(body.getPosition()).length()/Car.feelerLength);
		this.point=point;
		return 0;
	}
	
	public void reset(){
		length=new Double(0);
	}
	
	public Double getLength(){
		return length;
	}
	
	public Vec2 getPoint(){
		return point;
	}
	
	
}

