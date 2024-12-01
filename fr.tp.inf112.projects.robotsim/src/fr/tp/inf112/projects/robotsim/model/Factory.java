package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

@JsonIdentityInfo(
		generator = ObjectIdGenerators.PropertyGenerator.class,
		property = "id")
public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;
	
	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	//@JsonManagedReference
    private final List<Component> components;

	//@JsonIgnore
	//private transient List<Observer> observers;

	@JsonIgnore
	private transient boolean simulationStarted;
	
	@JsonIgnore 
	private transient FactoryModelChangedNotifier notifier;
	
	public Factory() { //no-argument constructor as required by Jackson
		this(0, 0, null);
	}
	
	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);
		
		components = new ArrayList<>();
		simulationStarted = false;
		notifier = new ObserverNotifier();
	}
	
	@JsonIgnore
	public FactoryModelChangedNotifier getNotifier() {
		return this.notifier;
	}
	
	@JsonIgnore
	public boolean setNotifier(FactoryModelChangedNotifier notifier2) {
		this.notifier = notifier2;
		return true;
	}
	
	@JsonIgnore
	public List<Observer> getObservers() {
		if(this.notifier instanceof ObserverNotifier) {
			return ((ObserverNotifier)this.notifier).getObservers();
		}
		else {
			return null;
		}		
		/*
		if (observers == null) {
			observers = new ArrayList<>();
		}		
		return observers;
		*/
	}

	@Override
	public boolean addObserver(Observer observer) {
		 if (notifier != null) {
			 return notifier.addObserver(observer);
		 }
		 return false;
		//return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
		if (notifier != null) {
			 return notifier.removeObserver(observer);
		 }
		return false;
		//return getObservers().remove(observer);
	}
	
	public void notifyObservers() {
		 if (notifier != null) {
			 notifier.notifyObservers();
		 }
		// for (final Observer observer : getObservers()) {
		// observer.modelChanged();
		// }
		}

	
	public boolean addComponent(final Component component) {
		if (components.add(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	//@JsonIgnore
	public List<Component> getComponents() {
		return components;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@JsonIgnore
	@Override
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}

	@Override
	public String toString() {
		return super.toString() + " components=" + components + "]";
	}
	
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	public void startSimulation() {
		if (!isSimulationStarted()) {
			this.simulationStarted = true;
			notifyObservers();

			/*while (isSimulationStarted()) {
				behave();
				
				try {
					Thread.sleep(100);
				}
				catch (final InterruptedException ex) {
					System.err.println("Simulation was abruptely interrupted");
				}
			}*/
			if(isSimulationStarted()) {
				behave();
			}
		}
	}

	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		boolean behaved = true;
		/*
		for (final Component component : getComponents()) {
			behaved = component.behave() || behaved;
		}*/
		List<Thread> threads = new ArrayList<>();
		for(Component component : getComponents()) {
			Runnable task = () -> {
				component.run();
			};
			Thread thread = new Thread(task);
			threads.add(thread);
			thread.start();
		}
		for(Thread thread : threads) {
			try {
				thread.join();
			}
			catch(InterruptedException e) {
				
			}
		}
		
		return behaved;
	}
	
	public boolean checkMotionAvailability(Motion motion) {
		boolean positionAvailable = true;
		Position targetPosition = motion.getTargetPosition();
		for(Component component : getComponents()) {
			if(component.isMobile() && component.getPosition() == targetPosition) {
				positionAvailable = false;
				break;
			}
		}
		return positionAvailable;
	}
	
	@JsonIgnore
	public ComponentStyle getComponentStyle() {
		return (ComponentStyle)getStyle();
	}
	
	@JsonIgnore
	@Override
	public Style getStyle() {
		return DEFAULT;
	}
	
	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setSimulationStatus(boolean flag) {
		this.simulationStarted = flag;
		return;
	}
	
}
