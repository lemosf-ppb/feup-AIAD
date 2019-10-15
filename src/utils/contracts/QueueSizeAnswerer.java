package utils.contracts;

import agents.AbstractAgent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.io.IOException;

public class QueueSizeAnswerer extends ContractNetResponder {

    public QueueSizeAnswerer(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
        AbstractAgent abstractAgent = (AbstractAgent) myAgent;
        if (abstractAgent.getQueueManager() == null)
        {
            abstractAgent.setQueueManager(cfp.getSender());
        }

        Integer queueSpace = abstractAgent.getQueueSize();

        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(queueSpace);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reply;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        System.out.println(myAgent.getLocalName() + " was selected to take the next person!");

        ACLMessage reply = accept.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent("Will be done");
        ((AbstractAgent) myAgent).increaseQueueSize();

        return reply;
    }
}