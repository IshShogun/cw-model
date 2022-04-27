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
		ImmutableSet<Piece> remaining = ImmutableSet.of(mrX.piece());
		ImmutableList<LogEntry> log = ImmutableList.of();
		Board.GameState state = new MyGameState(setup,remaining,log,mrX,detectives);
		List<Model.Observer> observers = new ArrayList<>();
		return new ObserveModel(state,observers,mrX,detectives);
	}

}
	final class ObserveModel extends MyGameState implements Model {
		public GameState gameState;
		public List<Model.Observer> observers;
		public ObserveModel(GameState state,List<Model.Observer> observers, Player mrX, List<Player> detectives ){
			super(state.getSetup(),state.getPlayers(),state.getMrXTravelLog(),mrX,detectives);
			this.gameState = new MyGameState(state.getSetup(),state.getPlayers(),state.getMrXTravelLog(),mrX,detectives);
			this.observers = observers;
		}


		@Nonnull
		@Override
		public Board getCurrentBoard() {
			 return this.gameState;
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
			this.gameState = advance(move);
			observers.stream().forEach(observer -> {
				observer.onModelChanged(getCurrentBoard(), Observer.Event.MOVE_MADE);
				if(!getWinner().isEmpty())
					observer.onModelChanged(getCurrentBoard(), Observer.Event.GAME_OVER);
				if(gameState.getSetup().moves.size() == gameState.getMrXTravelLog().size()){
					observer.onModelChanged(getCurrentBoard(), Observer.Event.GAME_OVER);
				}

			});
		}


	}
