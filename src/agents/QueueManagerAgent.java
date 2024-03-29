package agents;

import jade.lang.acl.ACLMessage;
import utils.Utils;
import utils.contracts.QueueSizeQuery;

import java.util.Vector;

public class QueueManagerAgent extends AbstractAgent {

    public QueueManagerAgent() {
        setLuggageAgents(new Vector<>());
        setPeopleScanAgents(new Vector<>());
        setInspectorAgents(new Vector<>());
    }

    @Override
    protected void setup() {
        System.out.println("Hallo! manager-agent " + getAID().getName() + " is ready.");
        setServiceDescription("manager");

        findAvailableAgents();
        acceptNewAgents();
    }

    private void findAvailableAgents() {
        luggageAgents = Utils.findAvailableAgents(this, "luggage");
        peopleScanAgents = Utils.findAvailableAgents(this, "scan");
        inspectorAgents = Utils.findAvailableAgents(this, "inspector");
    }

    private void acceptNewAgents() {
        addBehaviour(Utils.lateSubscriptionFactoryMethod(this, "luggage"));
        addBehaviour(Utils.lateSubscriptionFactoryMethod(this, "scan"));
        addBehaviour(Utils.lateSubscriptionFactoryMethod(this, "inspector"));
    }

    public void allocateLuggage() {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setContent("How many luggage can you receive?");
        addBehaviour(new QueueSizeQuery(this, msg, "luggage"));
    }

    public boolean isSystemReady(){
        return !peopleScanAgents.isEmpty() && !luggageAgents.isEmpty() && !inspectorAgents.isEmpty();
    }
}
