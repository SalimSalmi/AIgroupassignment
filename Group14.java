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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * This is your negotiation party.
 */
public class Group14 extends AbstractNegotiationParty {

	private static final Logger LOGGER = Logger.getLogger( Group14.class.getName() );

	private final float OPPONENT_MODEL_TIME = 0.2f; // Deadline to fix the opponent model
	private final float MEAN_MODEL_TIME = 0.35f; // Deadline to start calculating the mean model
	private final float CONCEDE_TIME = 0.9f; // Deadline to hard concede
	private final int REFRESH_MEAN = 20; // Amount of times the mean model is being refreshed
	private float nextRefresh = MEAN_MODEL_TIME; // Next time the mean model needs to be calculated.

	private final int BLOCK_SIZE = 5; // Size of bids to consider when calculating the concession.
	private final int RANDOM_SAMPLE = 30;

	private final float MINIMUM_UTILITY_START = 0.9f;
	private final float MINIMUM_UTILITY_END = 0.0f;
	private final float CONCESSION_CURVE = 30;

	//The state of the negotiation we are in, will change depending on the time left.
	private NegotiationState STATE = NegotiationState.OPPONENT_MODELING;

	private Bid lastReceivedBid = null;

	private OpponentList opponents = new OpponentList(BLOCK_SIZE); // List of opponent models
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

		try {
			Handler[] handlers = LOGGER.getHandlers();

			for(int i = 0; i < handlers.length; i++) {
				LOGGER.removeHandler(handlers[i]);
			}

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Calendar cal = Calendar.getInstance();
			Handler fh = new FileHandler("%h/Projects/java/logs/ai2016/group14-" + dateFormat.format(cal.getTime()) +" .log");
			LOGGER.addHandler(fh);

			NegotiationLogFormatter formatter = new NegotiationLogFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}

		minimumUtility = new MinimumUtility(MINIMUM_UTILITY_START, MINIMUM_UTILITY_END, CONCESSION_CURVE);
		acceptanceStrategy = new AcceptanceStrategy(utilSpace, minimumUtility, opponents);
		biddingStrategy = new BiddingStrategy(utilSpace, opponents);

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
				|| !acceptanceStrategy.accept(lastReceivedBid, bid)) {

			LOGGER.info( "Offer, " + getUtility(bid));

			return new Offer(getPartyId(), bid);

		} else {

			LOGGER.info( "Accept, " + getUtility(lastReceivedBid));

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
				opponent.pushBid(bid,getUtility(bid));
			}

			if (action instanceof Accept) {
				Bid bid = ((Accept) action).getBid();
				opponent.pushBid(bid,getUtility(bid));
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
			minimumUtility.set(time, opponents.getConcessionRate());
		} else {
			minimumUtility.set(time, 0);
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
	}

	private ArrayList<Bid> getNRandomBids(int sampleSize){
		ArrayList<Bid> bids = new ArrayList<>();

		// Always add the max utility bid
		try {
			bids.add(getUtilitySpace().getMaxUtilityBid());
		} catch (Exception e) {
			e.printStackTrace();
		}

		int i = 0;
		do {
			Bid bid = generateRandomBid();
			if(getUtility(bid) > minimumUtility.get()) {
				bids.add(bid);
			}
			i++;
		} while (bids.size() <= sampleSize && i < 10000);

		System.out.println("It took " + i + " loops with min value: " + minimumUtility.get());

		return bids;
	}
}
