package com.HOPn.AI_Pass.mockAgent;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AgentRegistry {

    private final List<Agent> agents;
    private final Map<String, Agent> byName = new HashMap<>();

    public AgentRegistry(List<Agent> agents) {
        this.agents = agents;
    }

    @PostConstruct
    void index() {
        for (Agent a : agents) {
            byName.put(a.getName().toLowerCase(), a);
        }
    }

    public Optional<Agent> find(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public List<String> listNames() {
        return agents.stream().map(Agent::getName).sorted().toList();
    }
}