package fr.tp.inf112.projects.robotsim.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.canvas.model.Shape;

@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
public abstract class Component implements Figure, Serializable, Runnable {
	
	private static final long serialVersionUID = -5960950869184030220L;

	private String id;

	@JsonBackReference
	private final Factory factory;
	
	private final PositionedShape positionedShape;
	
	private final String name;
	
	public Component() { //no-argument constructor as required by Jackson
		this(null, null, "DEFAULT");
	}

	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}
	
	public void run() {
		while(this.isSimulationStarted()) {
			try {
				this.behave();
				Thread.sleep(50);
			}
			catch(InterruptedException e) {
				
			}
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PositionedShape getPositionedShape() {
		return positionedShape;
	}
	
	@JsonIgnore
	public Position getPosition() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? null: shape.getPosition();
	}

	protected Factory getFactory() {
		return factory;
	}

	@JsonIgnore
	@Override
	public int getxCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getxCoordinate();
	}
	
	@JsonIgnore
	protected boolean setxCoordinate(int xCoordinate) {
		if ( getPositionedShape().setxCoordinate( xCoordinate ) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	@JsonIgnore
	@Override
	public int getyCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getyCoordinate();
	}

	@JsonIgnore
	protected boolean setyCoordinate(final int yCoordinate) {
		if (getPositionedShape().setyCoordinate(yCoordinate) ) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	protected void notifyObservers() {
		getFactory().notifyObservers();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", positionedShape=" + getPositionedShape();
	}
	
	@JsonIgnore
	public int getWidth() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getWidth();
	}

	@JsonIgnore
	public int getHeight() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getHeight();
	}
	
	public boolean behave() {
		return false;
	}
	
	@JsonIgnore
	public boolean isMobile() {
		return false;
	}
	
	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}
	
	public boolean overlays(final PositionedShape shape) {
		return getPositionedShape().overlays(shape);
	}
	
	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}
	
	@Override
	@JsonIgnore
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}
	
	@JsonIgnore
	@Override
	public Shape getShape() {
		return getPositionedShape();
	}
	
	@JsonIgnore
	public boolean isSimulationStarted() {
		return getFactory().isSimulationStarted();
	}
}
