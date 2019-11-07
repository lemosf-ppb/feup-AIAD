package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import models.Person;
import utils.Utils;
import utils.contracts.ClosestInspectorQuery;
import utils.contracts.QueueSizeAnswerer;

import java.util.Vector;

public class LuggageAgent extends AbstractAgent {

    private Vector<AID> inspectorAgents;

    public LuggageAgent() {
        setPeopleScanAgents(new Vector<>());
        state = State.IDLE;
    }

    @Override
    protected void setup() {
        System.out.println("Hallo! Luggage-agent " + getAID().getName() + " is ready.");
        setServiceDescription("luggage");

        findAvailableAgents();
        acceptNewAgents();

        addBehaviour(new QueueSizeAnswerer(this, MessageTemplate.MatchPerformative(ACLMessage.CFP),
                Utils.MAX_LUGGAGE_CAPACITY));
        addBehaviour(new ScanLuggage());
    }

    private void findAvailableAgents() {
        peopleScanAgents = Utils.findAvailableAgents(this, "scan");
        inspectorAgents = Utils.findAvailableAgents(this, "inspector");
    }

    private void acceptNewAgents() {
        addBehaviour(Utils.lateSubscriptionFactoryMethod(this, "scan"));
        addBehaviour(Utils.lateSubscriptionFactoryMethod(this, "inspector"));
    }

    private void allocateClosestInspector(Agent agent) {
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setContent("Which Inspector is the closest?");
        agent.addBehaviour(new ClosestInspectorQuery(agent, msg));
    }

    public Vector<AID> getInspectorAgents() {
        return inspectorAgents;
    }

    private class ScanLuggage extends CyclicBehaviour {
        private Person person;

        public void action() {

            if (state == State.IDLE) {

                if (agentQueue.isEmpty()) {
                    block();
                } else {
                    state = State.WORKING;

                    System.out.println(myAgent.getLocalName() + ": Going to start scanning the luggage of Person (ID: "
                            + ((Person) agentQueue.peek()).getId() + ")");

                    myAgent.addBehaviour(
                            new WakerBehaviour(myAgent, Utils.getMilliSeconds(Utils.LUGGAGE_PROCESSING_TIME)) {
                                @Override
                                protected void onWake() {
                                    person = (Person) agentQueue.peek();


                                    if (person.getHasIrregularLuggage()) {
                                        state = State.ALLOCATING;
                                        allocateClosestInspector(myAgent);
                                    } else {
                                        System.out.println(myAgent.getLocalName()
                                                + ": Finished scanning the luggage of Person (ID: "
                                                + ((Person) agentQueue.peek()).getId() + ")");
                                        state = State.IDLE;
                                        person = null;
                                        agentQueue.poll();
                                    }

                                }
                            });
                }
            } else if (state == State.ALLOCATING) {
                if (agentQueue.isEmpty() || !agentQueue.peek().equals(person)) {
                    System.out.println(myAgent.getLocalName()
                            + ": Delivered Person (ID: "
                            + person.getId() + ")");
                    state = State.IDLE;
                    person = null;

                } else {
                    block();
                }
            }
        }
    }
}
