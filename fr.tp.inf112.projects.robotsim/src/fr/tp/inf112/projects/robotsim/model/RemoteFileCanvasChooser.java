package fr.tp.inf112.projects.robotsim.model;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.remote.*;

public class RemoteFileCanvasChooser extends FileCanvasChooser {
	private final FileNameExtensionFilter fileNameFilter;

	public RemoteFileCanvasChooser(String fileExtension, String documentTypeLabel) {
		this(null, fileExtension, documentTypeLabel);
		// TODO Auto-generated constructor stub
	}

	public RemoteFileCanvasChooser(Component viewer, String fileExtension, String documentTypeLabel) {
		super(viewer, fileExtension, documentTypeLabel);
		fileNameFilter = new FileNameExtensionFilter(documentTypeLabel + " files " + "(*." + fileExtension + ")", fileExtension);
	}
	
	@Override
	protected String browseCanvases(final boolean open) {
		final int GET_FILES = 23; // Defining a instruction # to ask the server for returning file list
		String fileName = null;
	    if (open) { // query the server for file list and let user to select
	    	RemoteClientUtils client = new RemoteClientUtils("localhost", 65500, 1000);
	    	client.sendObject2Server(GET_FILES);
	    	List<String> fileList = (List<String>) client.recvObjectFromServer();
	    	if(fileList != null) {
	    		fileName = JOptionPane.showInputDialog("Select a Canvas to Open:", fileList);
	    	}
	    	else { // in case of no file found
	    		JOptionPane.showConfirmDialog(null, "No Files available!");
	    	}
	    }
	    else { // let user enter a filename to save to
	    	fileName = JOptionPane.showInputDialog("Enter Your Filename to Save:\n", ".factory");
	    }
	    
	    if (fileName != null && !fileName.trim().isEmpty()) {
		    File file = new File("./" + fileName);
		    if ((open && file.exists()) || !open) {
		    	return file.getPath();
		    }
		    
	    }
	    
	    return null;
	}

}
