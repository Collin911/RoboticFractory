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

public class RemotePersistenceManager extends AbstractCanvasPersistenceManager {

	public RemotePersistenceManager(CanvasChooser canvasChooser) {
		super(canvasChooser);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Canvas read(String canvasId) throws IOException {
		String host = "localhost";
		int port = 65500;
		int timeout = 1000;
		Socket socket = this.establishLink(host, port, timeout);
		if (socket != null) {
			try { // Send the CanvasId for request
					OutputStream sockOutStream = socket.getOutputStream();
					OutputStream bufOutStream = new BufferedOutputStream(sockOutStream);
					ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
					
					objOutStream.writeObject(canvasId);
					objOutStream.flush();
				}
			catch (IOException ex) {
				throw new IOException(ex);
				}
			try { // Read the object sent from the server
					final InputStream inStream = socket.getInputStream();
					final BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
					final ObjectInputStream objectInputStrteam = new ObjectInputStream(bufferedInputStream); 
					return (Canvas) objectInputStrteam.readObject();
				}
			catch (ClassNotFoundException | IOException ex) {
				throw new IOException(ex);
				}
			finally {
				try {
					socket.close();
				}
				catch(IOException ex) {
					
				}
			}
		}
		return null;
	}
	
	private Socket establishLink(String host, int port, int timeout) {
		try {
			InetAddress netAddr = InetAddress.getByName(host);
			SocketAddress sockAddr = new InetSocketAddress(netAddr, port);
			Socket socket = new Socket();
			socket.connect(sockAddr, timeout);
			return socket;
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @throws IOException 
	 */
	@Override
	public void persist(Canvas canvasModel) 
	throws IOException{
		String host = "localhost";
		int port = 65500;
		int timeout = 1000;
		Socket socket = this.establishLink(host, port, timeout);
		if (socket != null) {
			try (
				OutputStream sockOutStream = socket.getOutputStream();
				OutputStream bufOutStream = new BufferedOutputStream(sockOutStream);
				ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
			) {
				objOutStream.writeObject(canvasModel);
			}			
		}
	}


	@Override
	public boolean delete(Canvas canvasModel) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
