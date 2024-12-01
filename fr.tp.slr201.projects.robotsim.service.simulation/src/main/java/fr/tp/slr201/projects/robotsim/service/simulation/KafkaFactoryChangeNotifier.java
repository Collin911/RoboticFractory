package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryModelChangedNotifier;

public class KafkaFactoryChangeNotifier implements FactoryModelChangedNotifier {
	private Factory factoryModel;
	private KafkaTemplate<String, Factory> simulationEventTemplate;
	private NewTopic topic;

	public KafkaFactoryChangeNotifier(Factory fac, KafkaTemplate<String, Factory> simEventTemp) {
		this.factoryModel = fac;
		this.topic = TopicBuilder.name("simulation-" + factoryModel.getId()).build();
		this.simulationEventTemplate = simEventTemp;
	}

	public Factory getFactory() {
		return this.factoryModel;
	}

	@Override
	public void notifyObservers() {
		final Message<Factory> factoryMessage = MessageBuilder.withPayload(factoryModel)
				.setHeader(KafkaHeaders.TOPIC, "simulation-" + factoryModel.getId())
				.build();
		final CompletableFuture<SendResult<String, Factory>> sendResult =
				simulationEventTemplate.send(factoryMessage);
		sendResult.whenComplete((result, ex) -> {
			if (ex != null) {
				throw new RuntimeException(ex);
			}
		});

	}

	@Override
	public boolean addObserver(Observer observer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeObserver(Observer observer) {
		// TODO Auto-generated method stub
		return false;
	}

}


