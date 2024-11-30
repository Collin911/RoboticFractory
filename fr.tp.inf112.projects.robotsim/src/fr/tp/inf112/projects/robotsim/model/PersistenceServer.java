package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedOutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;
import java.util.ArrayList;

public class PersistenceServer {
	private static final Logger LOGGER = Logger.getLogger(PersistenceServer.class.getName());
	
    public static void main(String[] args) {
        int port = 65500; // Server will listen on this port

        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	LOGGER.config("Server started on port " + port);

            while (true) {
                // Accept incoming client connections
                Socket socket = serverSocket.accept();
                // Handle the connection in a separate thread
                Runnable reqProcessor = new PersistenceRequestProcessor(socket);
                new Thread(reqProcessor).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PersistenceRequestProcessor implements Runnable {
    private Socket socket;
    private static final Logger LOGGER = Logger.getLogger(PersistenceRequestProcessor.class.getName());

    public PersistenceRequestProcessor(Socket socket) {
        this.socket = socket;
    }
    
    private List<String> getAllFiles(String extension){
    	List<String> fileNames = new ArrayList<>(); // List to hold the filenames

        // Get the current directory
        File currentDir = new File("."); 

        // Define a FilenameFilter to filter files by the specified extension
        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith("." + extension.toLowerCase());

        // List all files in the current directory that match the filter
        String[] matchingFiles = currentDir.list(filter);
        // String[] files = currentDir.list();

        if (matchingFiles != null) {
            // Add matching file names to the list
            for (String fileName : matchingFiles) {
                fileNames.add(fileName);
            }
        }

        return fileNames; // Return the list of filenames
    }

    
    @Override
    public void run() {
        try {
            // Read request
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);

            Object recvObject = objectInputStream.readObject();

            if (recvObject instanceof String){ // Assuming read() is called and send back
                String canvasId = (String) recvObject;
                ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream);
                LOGGER.info("A string received, deserializing from designated file...");
                try {
                        final InputStream fileInputStream = new FileInputStream(canvasId);
                        final InputStream bufInputStream = new BufferedInputStream(fileInputStream);
                        final ObjectInputStream localObjectInputStrteam = new ObjectInputStream(bufInputStream);
                	Canvas canvas = (Canvas) localObjectInputStrteam.readObject();
                    objOutputStream.writeObject(canvas);
                    objOutputStream.flush();
                }
                catch (ClassNotFoundException | IOException ex) {
                    throw new IOException(ex);
                }
            }
            else if (recvObject instanceof Canvas) {
                Canvas canvas = (Canvas) recvObject;
                LOGGER.info("A canvas received, serializing to a file...");
                try (
                        final OutputStream fileOutStream = new FileOutputStream(canvas.getId());
                        final OutputStream bufOutStream = new BufferedOutputStream(fileOutStream);
                        final ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
                ) {
                    objOutStream.writeObject(canvas);
                }
            }
            else if (recvObject instanceof Integer) {
            	LOGGER.info("An instruction received, returning a list of filenames...");
            	List<String> fileList = (List<String>)getAllFiles("factory");
            	try (
            			ObjectOutputStream objOutputStream = new ObjectOutputStream(outStream);
                ) {
            		objOutputStream.writeObject(fileList);
            	}	
            }
        } catch (IOException e) {
           LOGGER.severe(e.getMessage() + e.getStackTrace());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            	LOGGER.severe(e.getMessage() + e.getStackTrace());
            }
        }
    }
}