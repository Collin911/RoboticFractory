package fr.tp.inf112.projects.robotsim.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.logging.Logger;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;

public class RemoteClientUtils {
	private String host;
	private int port;
	private int timeout;
	private Socket socket;
	private static final Logger LOGGER = Logger.getLogger(RemoteClientUtils.class.getName());
	
	public RemoteClientUtils(String host, int port, int timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		this.socket = this.establishLink();		
	}

	private Socket establishLink() {
		try {
			InetAddress netAddr = InetAddress.getByName(host);
			SocketAddress sockAddr = new InetSocketAddress(netAddr, port);
			Socket socket = new Socket();
			socket.connect(sockAddr, timeout);
			return socket;
		} catch (UnknownHostException e) {
			LOGGER.severe("Unknown host");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.severe(e.getMessage());
		}
		return null;
	}
	public boolean sendObject2Server(Object object) {
		if (socket != null) {
			try { // Send the CanvasId for request
					OutputStream sockOutStream = socket.getOutputStream();
					OutputStream bufOutStream = new BufferedOutputStream(sockOutStream);
					ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
					
					objOutStream.writeObject(object);
					objOutStream.flush();
				}
			catch (IOException e) {
				LOGGER.severe("An IOException is caught by remoteClientUtil when sending...");
				LOGGER.severe(e.getMessage());
			}
		}
		
		return false;
	}
	public Object recvObjectFromServer() {
		if (socket != null) {
			try { // Read the object sent from the server
				final InputStream inStream = socket.getInputStream();
				final BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
				final ObjectInputStream objectInputStrteam = new ObjectInputStream(bufferedInputStream); 
				return objectInputStrteam.readObject();
			}
			catch (IOException e) {
					LOGGER.severe("An IOException is caught by remoteClientUtil when recving...");
					LOGGER.severe(e.getMessage());
				}
			catch (ClassNotFoundException e) {
				LOGGER.severe("An ClassNotFoundException is caught by remoteClientUtil when recving...");
				LOGGER.severe(e.getMessage());
			}
		}
		return null;
	}
	
	public boolean closeConnection() {
		try {
			socket.close();
			return true;
			}
		catch(IOException ex) {
			return false;
		}
	}

}
