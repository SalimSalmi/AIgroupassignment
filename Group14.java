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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is your negotiation party.
 */
public class Group14 extends AbstractNegotiationParty {

	private static final Logger LOGGER = Logger.getLogger( Group14.class.getName() );

	private final float OPPONENT_MODEL_TIME = 0.2f; // Deadline to fix the opponent model
	private final float MEAN_MODEL_TIME = 0.35f; // Deadline to start calculating the mean model
	private final float CONCEDE_TIME = 0.99f; // Deadline until hard concede
	private final int REFRESH_MEAN = 50; // Amount of times the mean model is being refreshed
	private float nextRefresh = MEAN_MODEL_TIME; // Next time the mean model needs to be calculated.

	private final int RANDOM_SAMPLE = 30; // Amount of random samples we try to get above the min utility
	private final int SAMPLE_BOUND = 2500; // Amount of runs we maximally do to get the random samples.

	private final float MINIMUM_UTILITY_START = 0.9f; // Starting utility
	private final float MINIMUM_UTILITY_END = 0.0f; // Ending utility
	private final float CONCESSION_CURVE = 10; // Concession speed

	//The state of the negotiation we are in, will change depending on the time left.
	private NegotiationState STATE = NegotiationState.OPPONENT_MODELING;

	private Bid lastReceivedBid = null;
	private Bid maxBid;

	private OpponentList opponents = new OpponentList(); // List of opponent models
	private AcceptanceStrategy acceptanceStrategy; // Functions for the acceptance strategy
	private BiddingStrategy biddingStrategy; // Decides which bid to get next.
	private MinimumUtility minimumUtility; // The function for deciding the minimum required utility based on the time.

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
		acceptanceStrategy = new AcceptanceStrategy(utilSpace, minimumUtility);
		biddingStrategy = new BiddingStrategy(utilSpace, minimumUtility, opponents);

		try {
			maxBid = utilSpace.getMaxUtilityBid();
		} catch (Exception e) {
			e.printStackTrace();
		}

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

		Bid bid = biddingStrategy.getNextBid(STATE, getNRandomBids(RANDOM_SAMPLE));

		if (lastReceivedBid == null || !validActions.contains(Accept.class)
				|| !acceptanceStrategy.accept(lastReceivedBid, bid, STATE)) {

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

		generateRandomBid();


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

		float refreshDelta = (1 - MEAN_MODEL_TIME) / REFRESH_MEAN;

		if(STATE == NegotiationState.CONCEDING || STATE == NegotiationState.DEADLINE) {
			minimumUtility.set(time);
		} else {
			minimumUtility.set(time);
		}


		if(time > CONCEDE_TIME) {
			STATE = NegotiationState.DEADLINE;
		} else if (time > MEAN_MODEL_TIME) {
			STATE = NegotiationState.CONCEDING;
		} else if (time > OPPONENT_MODEL_TIME) {
			STATE = NegotiationState.MEAN_MODELING;
		}

		if(time > nextRefresh) {
			biddingStrategy.updateAverageOpponent();
			nextRefresh += refreshDelta;
		}

		if(time > MEAN_MODEL_TIME) {
			minimumUtility.minDistance(opponents.getRelativeDistance(maxBid));
		}
	}

	private ArrayList<Bid> getNRandomBids(int sampleSize){
		ArrayList<Bid> bids = new ArrayList<>();

		// Always add the max utility bid
		bids.add(maxBid);

		int i = 0;
		do {
			Bid bid = generateRandomBid();
			if(getUtility(bid) > minimumUtility.get()) {
				bids.add(bid);
			}
			i++;
		} while (bids.size() <= sampleSize && i < SAMPLE_BOUND);
		
		return bids;
	}
}
