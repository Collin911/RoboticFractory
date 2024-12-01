package fr.tp.inf112.projects.robotsim.model;

import fr.tp.inf112.projects.canvas.controller.Observer;

public interface FactoryModelChangedNotifier {
	
	public void notifyObservers();
	public boolean addObserver(Observer observer);
	public boolean removeObserver(Observer observer);

}
