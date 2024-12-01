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
	private String factoryID;
	private static final int START = 1;
	private static final int STOP = 2;
	private static final int CHECK = 3;
	private static final Logger LOGGER = Logger.getLogger(RemoteSimulatorController.class.getName());
	private Thread canvasDeamon;
	
	public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager) {
		this(null, persistenceManager);
	}
	
	public RemoteSimulatorController(String factoryID, final CanvasPersistenceManager persistenceManager) {
		super(persistenceManager);
		this.httpClient = HttpClient.newHttpClient();
		this.factoryID = factoryID;
		this.factoryModel = getFactory();
		this.canvasDeamon= new Thread(() -> {
            while(true) {
            	try {
					updateViewer();
					Thread.sleep(100);
				} catch (InterruptedException | URISyntaxException | IOException e) {
					LOGGER.severe(e.getMessage() + e.getStackTrace());
				}
            	
            }
        });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startAnimation() {
		this.animationControl(START);
		canvasDeamon.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopAnimation() {
		this.animationControl(STOP);
		stopCanvasDeamon();
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
		fac.setSimulationStatus(isAnimationRunning());
		return fac;
	}
	
	private void updateLocalFactoryWith(Factory fac) {
		// Use this method to update the local model so that you won't lose your observer/notifier
		
		//SRP setup
		fac.setNotifier(this.factoryModel.getNotifier());
		
		// Vanilla setup
		/*
		for(Observer ob : this.factoryModel.getObservers()) {
			fac.addObserver(ob);
		}
		this.factoryModel = fac;
		*/
		return;
	}
	
	private void stopCanvasDeamon() {
		if(canvasDeamon!=null & canvasDeamon.isAlive()) {
			canvasDeamon.interrupt();
		}
	}
	
	private void updateViewer() throws InterruptedException, URISyntaxException, IOException {
		Factory fac = getFactory();
		 while (fac.isSimulationStarted()) {
			 updateLocalFactoryWith(fac);
			 final Factory remoteFactoryModel = getFactory();
			 setCanvas(remoteFactoryModel);
		 }
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public void setCanvas(final Canvas canvasModel) {
		final List<Observer> observers = this.factoryModel.getObservers();
		super.setCanvas(canvasModel);
		this.factoryModel = (Factory) canvasModel;
		for (final Observer observer : observers) {
			this.factoryModel.addObserver(observer);
		}
		this.factoryModel.notifyObservers();
	}

}
