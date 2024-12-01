package fr.tp.slr201.projects.robotsim.service.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.app.SimulationServiceUtils;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

@Configuration
public class SimulationServiceConfig {
	 @Bean
	 @Primary
	 ObjectMapper objectMapper() {
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
		 objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
		 return objectMapper;
	 }

	 @Bean
	 ProducerFactory<String, Factory> producerFactory(){
		 final Map<String, Object> config = new HashMap<>();
		 config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SimulationServiceUtils.BOOTSTRAP_SERVERS);
		 final JsonSerializer<Factory> factorySerializer = new JsonSerializer<>(objectMapper());
		 return new DefaultKafkaProducerFactory<>(config,new StringSerializer(),factorySerializer);
	 }
	 
	 @Bean
	 @Primary
	 KafkaTemplate<String, Factory> kafkaTemplate(){
		 return new KafkaTemplate<>(producerFactory());
	 }
}

