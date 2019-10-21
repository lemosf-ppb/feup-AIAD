package utils.contracts;

import agents.LuggageAgent;
import agents.QueueManagerAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.Utils;

import java.util.Vector;

public class QueueSizeQuery extends ContractNetInitiator {

    String agentType;

    public QueueSizeQuery(Agent a, ACLMessage msg, String agentType) {
        super(a, msg);
        this.agentType = agentType;
    }

    @Override
    protected Vector prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> v = new Vector<>();

        QueueManagerAgent queueManagerAgent = null;
        LuggageAgent luggageAgent = null;
        if(myAgent instanceof QueueManagerAgent){
            queueManagerAgent = (QueueManagerAgent) myAgent;
        }
        else{
            luggageAgent = (LuggageAgent) myAgent;
        }

        switch(agentType){
            case "luggage":
                for (AID aid : ((QueueManagerAgent) myAgent).getLuggageAgents()) {
                    cfp.addReceiver(aid);
                }
                break;
            case "scan":

                if(luggageAgent != null){
                    for (AID aid : luggageAgent.getPeopleScanAgents()) {
                        cfp.addReceiver(aid);
                    }
                }
                else{
                    for (AID aid : queueManagerAgent.getPeopleScanAgents()) {
                        cfp.addReceiver(aid);
                    }
                }

                break;
        }

        v.add(cfp);
        return v;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {

        int maxQueueSize = 0;
        switch(agentType){
            case "luggage":
                maxQueueSize = Utils.MAX_LUGGAGE_CAPACITY;
                break;
            case "scan":
                maxQueueSize = Utils.MAX_PEOPLE_QUEUE_SIZE;
                break;
        }

        int min = maxQueueSize;
        for (Object response : responses) {
            int curr = maxQueueSize;
            try {
                curr = (Integer) ((ACLMessage) response).getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }

            if (curr < min) min = curr;
        }

        boolean chosen = false;
        for (Object response : responses) {
            ACLMessage current = (ACLMessage) response;
            try {
                ACLMessage msg = current.createReply();
                if (!chosen && (Integer) current.getContentObject() == min) {
                    msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    chosen = true;
                } else {
                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
                acceptances.add(msg);
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }
}