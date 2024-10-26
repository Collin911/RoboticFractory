package fr.tp.slr201.projects.robotsim.service.simulation;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.app.SimulatorController;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.RemoteFileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.RemotePersistenceManager;

@SpringBootApplication
@RestController
public class MicroController {
	// This server is for remote simulation
	// It runs on port 65501 by default
	
	private static final Logger LOGGER = Logger.getLogger(SimulatorApplication.class.getName());
	private List<Factory> factoryList;
	private List<Thread> simuProcessorList;
	final private RemoteFileCanvasChooser canvasChooser;
	final private RemotePersistenceManager remotePersistMgr;

	public static void main(String[] args) {
		SpringApplication.run(MicroController.class, args);
	}
	
	public MicroController(){
		factoryList = new ArrayList<Factory>();
		simuProcessorList = new ArrayList<Thread>();
		canvasChooser = new RemoteFileCanvasChooser("factory", "Puck Factory");
		remotePersistMgr = new RemotePersistenceManager(canvasChooser);
	}

	
	@GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
      return String.format("Hello %s!", name);
    }
	
	@GetMapping("/simulation/{factoryID}/start")
    public boolean startSimulation(@PathVariable String factoryID) {
		Factory factory = null;
		boolean returnVal = false;
		try {
			factory = (Factory) remotePersistMgr.read(factoryID);	
		} catch (IOException e) {
			LOGGER.info(String.format("The following IOException caught during getting factory %s.", factoryID));
			LOGGER.info(e.getMessage());
		}
		if (factory != null) {
			factoryList.add(factory);
			Runnable reqProcessor = new simulationRequestProcessor(factory);
            Thread simulationThread = new Thread(reqProcessor);
            simulationThread.start();
            simuProcessorList.add(simulationThread);
			// factory.startSimulation();
			returnVal = true;
			LOGGER.fine(String.format("Starting factory %s successfully!", factoryID));
	    }
		
		return returnVal;
	}
	
	@GetMapping("/simulation/{factoryID}/stop")
    public boolean stopSimulation(@PathVariable String factoryID) {
		Factory factory = getFactoryById(factoryID);
		boolean returnVal = false;
		if (factory != null) {
			factory.stopSimulation();
			returnVal = true;
			LOGGER.fine(String.format("Starting factory %s successfully!", factoryID));
	    }
		
		return returnVal;
	}
	
	@GetMapping("/simulation/{factoryID}/get")
    public Factory getFactory(@PathVariable String factoryID) {
		return getFactoryById(factoryID);
	}
	
	private Factory getFactoryById(String factoryID) {
		Factory returnVal = null;
		for (Factory fac : factoryList) {
			File file = new File(fac.getId());
	        String facID = file.getName();
			if(facID.equals(factoryID)) {
				returnVal = fac;
			}
		}	
		return returnVal;
	}
	
	private Thread getThreadById(String factoryID) {
		for (Thread thread : simuProcessorList) {
		}
	}
      
}

class simulationRequestProcessor implements Runnable {
	    protected Factory factory;

	    public simulationRequestProcessor(Factory factory) {
	        this.factory = factory;
	    }

	    
	    @Override
	    public void run() {
	    	factory.startSimulation();
	    	return;
	    	
	    }
	    
	    public void stop() {
	    	factory.stopSimulation();
	    	return;
	    }
	}
