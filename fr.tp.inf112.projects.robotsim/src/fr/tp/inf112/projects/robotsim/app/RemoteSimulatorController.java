package fr.tp.inf112.projects.robotsim.app;

import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.PersistenceServer;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public class RemoteSimulatorController extends SimulatorController{
	
	private HttpClient httpClient;	
	private final CanvasPersistenceManager persistenceManager;
	private String factoryID;
	private static final int START = 1;
	private static final int STOP = 2;
	private static final int CHECK = 3;
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
	
	public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager) {
		this(null, persistenceManager);
	}
	
	public RemoteSimulatorController(String factoryID, final CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
		this.persistenceManager = persistenceManager;
		this.httpClient = HttpClient.newHttpClient();
		this.factoryID = factoryID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAnimation() {
		this.animationControl(START);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
		this.animationControl(STOP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAnimationRunning() {
		return this.animationControl(CHECK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CanvasPersistenceManager getPersistenceManager() {
		return persistenceManager;
	}
	
	
	private boolean animationControl(int reqType) {
		// This method allows start/stop the simulation or check is simulation is running
		boolean returnVal = false;
		String operation;
		switch(reqType) {
			case 1:
				operation = "start";
				break;
			case 2:
				operation = "stop";
				break;
			case 3:
				operation = "status";
				break;
			default:
				return false;			
		}
		try {			
			final URI uri = new URI("http", null, "localhost", 65501, "/simulation/" + factoryID + "/" + operation, null, null);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			try {
				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				ObjectMapper objectMapper = new ObjectMapper();
				returnVal = objectMapper.readValue(response.body(), boolean.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.severe(e.getMessage() + e.getStackTrace());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOGGER.severe(e.getMessage() + e.getStackTrace());
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.severe(e.getMessage() + e.getStackTrace());
		}		
		
		return returnVal;
	}
	
	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		 while (((Factory)getCanvas()).isSimulationStarted()) {
		 final Factory remoteFactoryModel = getFactory();
		 setCanvas(remoteFactoryModel);
		 Thread.sleep(100);
		 }
	}

	private Factory getFactory() {
		Factory fac = null;
		try {			
			final URI uri = new URI("http", null, "localhost", 65501, "/simulation/" + factoryID + "/getJson", null, null);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			try {
				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				// System.out.println(response.body()); // For debug only
				ObjectMapper objectMapper = new ObjectMapper();
				// Adding polymorphic support
				PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
						 .allowIfSubType(PositionedShape.class.getPackageName())
						 .allowIfSubType(Component.class.getPackageName())
						 .allowIfSubType(BasicVertex.class.getPackageName())
						 .allowIfSubType(ArrayList.class.getName())
						 .allowIfSubType(LinkedHashSet.class.getName())
						 .allowIfSubType("fr.tp.inf112.projects.canvas.model.impl")
						 .allowIfSubType("fr.tp.inf112.projects.robotsim.model")
						 .allowIfBaseType("fr.tp.inf112.projects.robotsim.model")
					.build();
				objectMapper.activateDefaultTyping(typeValidator, 
						 ObjectMapper.DefaultTyping.NON_FINAL);

				fac = objectMapper.readValue(response.body(), Factory.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.severe(e.getMessage());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOGGER.severe(e.getMessage() + e.getStackTrace());
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.severe(e.getMessage() + e.getStackTrace());
		}
		return fac;
	}

	@Override
	public boolean addObserver(Observer observer) {
		Factory fac = getFactory();
		if (fac != null) {
			return fac.addObserver(observer);
		}
		
		return false;
	}

	@Override
	public boolean removeObserver(Observer observer) {
		Factory fac = getFactory();
		if (fac != null) {
			return fac.removeObserver(observer);
		}
		
		return false;
	}

	@Override
	public Canvas getCanvas() {
		return (Canvas)this.getFactory();
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setCanvas(final Canvas canvasModel) {
		 final List<Observer> observers = getFactory().getObservers();
		 super.setCanvas(canvasModel);
		 for (final Observer observer : observers) {
		 ((Factory) getCanvas()).addObserver(observer);
		 }
		 ((Factory) getCanvas()).notifyObservers();
	}
	
}
