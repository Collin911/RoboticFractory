package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.remote.*;


public class RemotePersistenceManager extends AbstractCanvasPersistenceManager {

	public RemotePersistenceManager(CanvasChooser canvasChooser) {
		super(canvasChooser);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Canvas read(String canvasId) throws IOException {
		RemoteClientUtils client = new RemoteClientUtils("localhost", 65500, 1000);
		Canvas result = null;
		if (client != null) {
			client.sendObject2Server(canvasId);
			result =  (Canvas) client.recvObjectFromServer();
			client.closeConnection();
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IOException 
	 */
	@Override
	public void persist(Canvas canvasModel) throws IOException{
		RemoteClientUtils client = new RemoteClientUtils("localhost", 65500, 1000);
		client.sendObject2Server(canvasModel);
		client.closeConnection();
	}


	@Override
	public boolean delete(Canvas canvasModel) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
