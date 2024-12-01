package fr.tp.inf112.projects.robotsim.model;

import java.util.ArrayList;
import java.util.List;

import fr.tp.inf112.projects.canvas.controller.Observer;

public class ObserverNotifier implements FactoryModelChangedNotifier {
	private transient List<Observer> observers;
	
	public List<Observer> getObservers() {
		if (observers == null) {
			observers = new ArrayList<>();
		}
		
		return observers;
	}

	public ObserverNotifier() {
		this(null);
	}
	
	public ObserverNotifier(List<Observer> observers){
		this.observers = observers;
	}

	@Override
	public void notifyObservers() {
		for (final Observer observer : getObservers()) {
			observer.modelChanged();
		}
	}

	@Override
	public boolean addObserver(Observer observer) {
		return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
		return getObservers().remove(observer);
	}

}
