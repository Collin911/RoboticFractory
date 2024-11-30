package fr.tp.inf112.projects.robotsim.model.shapes;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import fr.tp.inf112.projects.canvas.model.Shape;
import fr.tp.inf112.projects.robotsim.model.Position;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, 
	    include = JsonTypeInfo.As.PROPERTY, 
	    property = "type"
	)
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = RectangularShape.class, name = "RectangularShape"),
	    @JsonSubTypes.Type(value = CircularShape.class, name = "CircularShape"),
	    @JsonSubTypes.Type(value = BasicPolygonShape.class, name = "BasicPolygonShape")
	})
public abstract class PositionedShape implements Shape, Serializable {

	private static final long serialVersionUID = 2217860927757709195L;

	private static float intersectionLength(final int coordinate1,
											final int width1,
											final int coordinate2,
											final int width2) {
		if (coordinate1 <= coordinate2) {
			if (coordinate1 + width1 >= coordinate2) {
				return coordinate1 + width1 - coordinate2;
			}
			
			return 0;
		}

		if (coordinate2 + width2 >= coordinate1) {
			return coordinate2 + width2 - coordinate1;
		}
		
		return 0;
	}

	private final Position position;
	
	//empty constructor
	protected PositionedShape() {
		this(-1, -1);
	}
	
	protected PositionedShape(final int xCoordinate,
							  final int yCoordinate) {
		this.position = new Position(xCoordinate, yCoordinate);
	}

	public abstract int getWidth();

	public abstract int getHeight();
	
	public boolean overlays(final PositionedShape shape) {
		return getOverlayedSurface(shape) > 0.0f;
	}
	
	public float getOverlayedSurface(final PositionedShape shape) {
		return xIntersectionLength(shape) * yIntersectionLength(shape);
	}
	
	protected float xIntersectionLength(final PositionedShape shape) {
		return intersectionLength(getxCoordinate(), getWidth(), shape.getxCoordinate(), shape.getWidth());
	}

	protected float yIntersectionLength(final PositionedShape shape) {
		return intersectionLength(getyCoordinate(), getHeight(), shape.getyCoordinate(), shape.getHeight());
	}
	
	public Position getPosition() {
		return position;
	}

	@JsonIgnore
	public int getxCoordinate() {
		return getPosition().getxCoordinate();
	}

	@JsonIgnore
	public boolean setxCoordinate(final int xCoordinate) {
		return getPosition().setxCoordinate(xCoordinate);
	}
	
	@JsonIgnore
	public int getyCoordinate() {
		return getPosition().getyCoordinate();
	}

	@JsonIgnore
	public boolean setyCoordinate(final int yCoordinate) {
		return getPosition().setyCoordinate(yCoordinate);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " at " + String.valueOf(getPosition());
	}
}
