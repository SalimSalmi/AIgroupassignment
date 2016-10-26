package ai2016;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.util.List;

/**
 * This is your negotiation party.
 */
public class Group14 extends AbstractNegotiationParty {

	private final float OPPONENT_MODEL_TIME = 0.2f; // Deadline to fix the opponent model
	private final float MEAN_MODEL_TIME = 0.6f; // Deadline to start calculating the mean model
	private final float CONCEDE_TIME = 0.9f; // Deadline to hard concede
	private final int REFRESH_MEAN = 10; // Amount of times the mean model is being refreshed
	private Double nextRefresh; // Next time the mean model needs to be calculated.

	private final float MINIMUM_UTILITY_START = 0.9f;
	private final float MINIMUM_UTILITY_END = 0.5f;
	private final float CONCESSION_CURVE = 10;

	//The state of the negotiation we are in, will change depending on the time left.
	private NegotiationState STATE = NegotiationState.OPPONENT_MODELING;

	private Bid lastReceivedBid = null;

	private OpponentList opponents = new OpponentList(); // List of opponent models
	private AcceptanceStrategy acceptanceStrategy; // Functions for the acceptance strategy
	private BiddingStrategy biddingStrategy; // Decides which bid to get next.
	private MinimumUtility minimumUtility;

	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,
			TimeLineInfo tl, long randomSeed, AgentID agentId) {

		super.init(utilSpace, dl, tl, randomSeed, agentId);

		System.out.println("Discount Factor is "
				+ utilSpace.getDiscountFactor());
		System.out.println("Reservation Value is "
				+ utilSpace.getReservationValueUndiscounted());

		// if you need to initialize some variables, please initialize them
		// below

		minimumUtility = new MinimumUtility(MINIMUM_UTILITY_START, MINIMUM_UTILITY_END, CONCESSION_CURVE);
		acceptanceStrategy = new AcceptanceStrategy(utilSpace, minimumUtility, opponents);
		biddingStrategy = new BiddingStrategy(utilSpace, minimumUtility, opponents, MEAN_MODEL_TIME);

	}

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {

		double time = getTimeLine().getCurrentTime() / getTimeLine().getTotalTime();

		updateTimeAndState(time);

		if (lastReceivedBid == null || !validActions.contains(Accept.class)
				|| !acceptanceStrategy.accept(lastReceivedBid)) {

			Bid bid;

			switch(STATE) {
				case MEAN_MODELING:
					opponents.stopModeling();
				case OPPONENT_MODELING:
					bid = biddingStrategy.getNextHardHeaded(time);
					break;
				case CONCEDING:
					bid = biddingStrategy.getNextBid();
					break;
				case DEADLINE:
					bid = biddingStrategy.getNextBid();
					break;
				default:
					bid = biddingStrategy.getNextHardHeaded(time);
			}

			return new Offer(getPartyId(), bid);

		} else {

			return new Accept(getPartyId(), lastReceivedBid);
		}

	}

	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);

		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
		}

		if(sender != null) {
			OpponentModel opponent = opponents.getOpponent(sender, getUtilitySpace());

			if (action instanceof Offer) {
				Bid bid = ((Offer) action).getBid();
				opponent.pushBid(bid);
			}

			if (action instanceof Accept) {
				Bid bid = ((Accept) action).getBid();
				opponent.pushBid(bid);
			}
		}

	}


	@Override
	public String getDescription() {
		return "Party group 14 v0.0.10";
	}


	private void updateTimeAndState(double time){

		double refreshDelta = (1 - MEAN_MODEL_TIME) / REFRESH_MEAN;

		minimumUtility.set(time);

		if(nextRefresh == null) {
			nextRefresh = MEAN_MODEL_TIME + refreshDelta;
		} else if(nextRefresh < time) {
			biddingStrategy.updateAverageOpponent();
			nextRefresh += refreshDelta;
		}

		if(time > CONCEDE_TIME) {
			STATE = NegotiationState.DEADLINE;
		} else if (time > MEAN_MODEL_TIME) {
			STATE = NegotiationState.CONCEDING;
		} else if (time > OPPONENT_MODEL_TIME) {
			STATE = NegotiationState.MEAN_MODELING;
		}

	}
}
