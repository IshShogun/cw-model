package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory.MyGameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		// TODO
		throw new RuntimeException("Implement me!");


		return new ObserveModel(setup, , ImmutableList.of(), mrX, observers );
	}

}

	final class ObserveModel extends MyGameState implements Model {
		/*private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;*/
		private List<Model.Observer> observers;

		public ObserveModel(GameSetup setup, ImmutableSet<Piece> remaining, ImmutableList<LogEntry> log, Player mrX, List<Player> detectives) {
			super(setup, remaining, log, mrX, detectives);
		}

		//ImmutableList<Player> dec = ImmutableList.copyOf(detectives);

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			// getPlayerTickets();
			 return null;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			observers.add(observer);


		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			int observerIndex = observers.indexOf(observer);
			observers.remove(observerIndex);

		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			ImmutableSet<Model.Observer> ob = ImmutableSet.copyOf(observers);
			return ob;
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			// TODO Advance the model with move, then notify all observers of what what just happened.
			//  you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER
			for(Model.Observer observer : observers){
				observer.onModelChanged(getCurrentBoard(), Model.Observer.Event.MOVE_MADE);
				if(!getWinner().isEmpty()) {
					observer.onModelChanged(getCurrentBoard(), Model.Observer.Event.GAME_OVER);
				}
			}

		}
	}
