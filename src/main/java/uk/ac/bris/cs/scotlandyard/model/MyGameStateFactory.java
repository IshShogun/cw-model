package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.sun.javafx.image.IntPixelGetter;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull
	@Override
	public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		if(detectives.equals(null)) {
			throw new IllegalArgumentException("Detectives null");
		}
		List<Integer> loc = new ArrayList<>();
		//int counter = 0;
		for(Player player : detectives){
			/*if(loc.contains(player.location()))
				throw new IllegalArgumentException("hella");*/
			if(player.has(ScotlandYard.Ticket.DOUBLE) || player.has(ScotlandYard.Ticket.SECRET))
				throw new IllegalArgumentException("Wrong tickets");
			//loc.add(player.location(), counter);
			//counter++;
		}
		for(int i = 0; i<detectives.size();i++){
			for(int j = i + 1; j < detectives.size(); j++){
				if(detectives.get(i).equals(detectives.get(j)))
					throw new IllegalArgumentException("duplicates");
			}
		}
		for(Player player: detectives){
			for(Player player2: detectives){
				if(!player.equals(player2) && player.location() == player2.location())
					throw new IllegalArgumentException("cant be in same place");
			}
		}
		if(setup.graph.nodes().isEmpty())
			throw new IllegalArgumentException("graph null");

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);


	}



	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;




		@Override
		public GameSetup getSetup() {

			return setup;
		}

		@Override
		public ImmutableSet<Piece> getPlayers() {
			HashSet<Piece> p = new HashSet<>();
			for (Player player : detectives) {
				p.add(player.piece());
			}
			p.add(mrX.piece());
			ImmutableSet<Piece> pl = ImmutableSet.copyOf(p);
			return pl;
		}

		@Override
		public GameState advance(Move move) {
			movesV m = new movesV();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			return (GameState) move.accept(m);
		}
		int getMrXLocation(){
			return mrX.location();
		}

		Player getPlayerFromPiece(Piece piece){
			for(Player player: detectives){
				if(player.piece().equals(piece))
					return player;
				/*else
					return mrX;*/
			}
			return mrX;
		}

		final class movesV implements Move.Visitor{

			@Override
			public GameState visit(Move.SingleMove move) {
				//TODO if its mr X turn add to log not destination
				if(move.commencedBy().isMrX()){
					//move mr X and update tickets
					mrX = mrX.use(move.ticket);
					mrX = mrX.at(move.destination);
					//update remaining
					HashSet<Piece> set = new HashSet();
					//TODO add all detectives
					for(Player detective: detectives){
						set.add(detective.piece());
					}
					//update log
					HashSet<LogEntry> log1 = new HashSet<>();
					log1.addAll(log);
					if(setup.moves.get(log.size())){
						LogEntry logEntry1 = LogEntry.reveal(move.ticket , mrX.location());
						log1.add(logEntry1);

					}else {
						LogEntry logEntry1 = LogEntry.hidden(move.ticket);
						log1.add(logEntry1);
					}

					ImmutableList<LogEntry> imLog = ImmutableList.copyOf(log1);
					ImmutableSet<Piece> im = ImmutableSet.copyOf(set);
					return new MyGameState(setup, im, log, mrX, detectives);

				}
				else{
					//update player location
					Player player = getPlayerFromPiece(move.commencedBy());
					player = player.at(move.destination);
					player = player.use(move.ticket);
					List<Player> detectivesNew = new ArrayList<>();
					//make new list of detectives with new location
						for(Player detective : detectives){
							if(detective.piece().webColour().equals(move.commencedBy().webColour()))
								detectivesNew.add(player);
							else
								detectivesNew.add(detective);
						}
					mrX = mrX.give(move.ticket);
					//update remaining
					HashSet<Piece> newRemaining = new HashSet<>(remaining);
					newRemaining.remove(move.commencedBy());
					if(newRemaining.isEmpty()){
						newRemaining.add(mrX.piece());
						ImmutableSet<Piece> imNewRemaining = ImmutableSet.copyOf(newRemaining);
						return new MyGameState(setup, imNewRemaining, log, mrX, detectivesNew);
					} else {
						ImmutableSet<Piece> imNewRemaining = ImmutableSet.copyOf(newRemaining);
						return new MyGameState(setup, imNewRemaining, log, mrX, detectivesNew);
					}
				}
			}

			@Override
			public GameState visit(Move.DoubleMove move) {
				//TODO check to turn to see if logEntry is revealed or hidden
				mrX = mrX.at(move.destination2);
				mrX = mrX.use(move.ticket1);
				mrX = mrX.use(move.ticket2);
				HashSet<LogEntry> log1 = new HashSet<>();
				HashSet<Piece> set = new HashSet<>();
				log1.addAll(log);
				if(setup.moves.get(log.size())){
					LogEntry logEntry1 = LogEntry.reveal(move.ticket2 , mrX.location());
					log1.add(logEntry1);

				}else {
					LogEntry logEntry1 = LogEntry.hidden(move.ticket2);
					log1.add(logEntry1);
				}

				ImmutableList<LogEntry> imLog = ImmutableList.copyOf(log1);
				ImmutableSet<Piece> im = ImmutableSet.copyOf(set);

				return new MyGameState(setup, im, imLog, mrX, detectives);
			}

		}








		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			int i = detectives.indexOf(getPlayerFromPiece(detective));
			if(i == -1){
				return Optional.empty();
			}
			return Optional.of(detectives.get(i).location());
		}


		class TB implements TicketBoard{
			int bus;
			int taxi;
			int underground;
			int x2;
			int secret;
			Player player;
			private TB(Piece piece){
				player = getPlayerFromPiece(piece);

				this.bus = player.tickets().get(ScotlandYard.Ticket.BUS);
				this.taxi = player.tickets().get(ScotlandYard.Ticket.TAXI);
				this.underground = player.tickets().get(ScotlandYard.Ticket.UNDERGROUND);
				this.x2 = player.tickets().get(ScotlandYard.Ticket.DOUBLE);
				this.secret = player.tickets().get(ScotlandYard.Ticket.SECRET);
			}
			@Override
			public int getCount(ScotlandYard.Ticket ticket){
				if(ticket.getClass().getName().equals(ScotlandYard.Ticket.BUS.getClass().getName()))
					return this.bus;
				if(ticket.getClass().getName().equals(ScotlandYard.Ticket.TAXI.getClass().getName()))
					return this.taxi;
				if(ticket.getClass().getName().equals(ScotlandYard.Ticket.DOUBLE.getClass().getName()))
					return this.x2;
				if(ticket.getClass().getName().equals(ScotlandYard.Ticket.UNDERGROUND.getClass().getName()))
					return this.underground;
				if(ticket.getClass().getName().equals(ScotlandYard.Ticket.SECRET.getClass().getName()))
					return this.x2;
				else
					return -1;
			}
		}





		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			TicketBoard ticketBoard = new TB(piece);
			Optional<TicketBoard> tb = Optional.of(ticketBoard);
			if(!piece.isMrX()){
				int i = detectives.indexOf(getPlayerFromPiece(piece));
				if(i == -1){
					return Optional.empty();
				}
			}
			return tb;
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {

			return log;

		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		List<Player> getRemainingPlayers(ImmutableSet<Piece> remaining){
			List<Player> remainingPlayers = new ArrayList<>();
			for(Piece piece : remaining){
				remainingPlayers.add(getPlayerFromPiece(piece));
			}
			return remainingPlayers;

		}
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			if(remaining.isEmpty()){
				ImmutableSet<Move> someoneWon = ImmutableSet.of();
				return someoneWon;
			}
			List<Player> remainingPlayers = getRemainingPlayers(remaining);
			HashSet<Move> moves = new HashSet<>();

			for(Player player: remainingPlayers){
				makeSingleMoves(setup, detectives, player, player.location());
				moves.addAll(makeSingleMoves(setup, detectives, player, player.location()));
				if(!(log.size() + 1 == setup.moves.size())) {
					makeDoubleMoves(setup, detectives, player, player.location());
					moves.addAll(makeDoubleMoves(setup, detectives, player, player.location()));
				}
			}
			ImmutableSet<Move> iMoves = ImmutableSet.copyOf(moves);
			return iMoves;
		}

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.moves = getAvailableMoves();
			if (setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");

			HashSet<Piece> detectivesWin = new HashSet<>();
			//HashSet<Piece> noWinners = new HashSet<>();
			for(Player player1 : detectives){
				detectivesWin.add(player1.piece());
			}
			this.winner = ImmutableSet.of();
			for (Player player : detectives) {
				if (mrX.location() == player.location() || getPlayerTickets(mrX.piece()).isEmpty()) {
					this.winner = ImmutableSet.copyOf(detectivesWin);
					ImmutableSet<Piece> emptyRemaining = ImmutableSet.of();
					//ImmutableSet<Move> emptyMove = ImmutableSet.of();
					this.remaining = emptyRemaining;
					//this.moves = emptyMove;
				}
				//if detectives run out of moves || if mr can't move anymore
				//from remaining
				HashSet<Move> availMoves = new HashSet<>();
				HashSet<Move> MrXavailMoves = new HashSet<>();
				for(Player player2 : detectives){
					availMoves.addAll(makeSingleMoves(setup,detectives,player2,player2.location()));
				}
				if(availMoves.isEmpty()) {
					this.winner = ImmutableSet.of(mrX.piece());
					ImmutableSet<Piece> emptyRemaining = ImmutableSet.of();
					this.remaining = emptyRemaining;
				}
				MrXavailMoves.addAll(makeSingleMoves(setup,detectives,mrX,mrX.location()));
				MrXavailMoves.addAll(makeDoubleMoves(setup,detectives,mrX,mrX.location()));
				if(MrXavailMoves.isEmpty()){
					this.winner = ImmutableSet.copyOf(detectivesWin);
					ImmutableSet<Piece> emptyRemaining = ImmutableSet.of();
					this.remaining = emptyRemaining;
				}
			}

		}

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player
				player, int source) {

			// TODO create an empty collection of some sort, say, HashSet, to store all the SingleMove we generate
			HashSet<Move.SingleMove> set = new HashSet<>();
			for (int destination : setup.graph.adjacentNodes(source)) {
				int check = checkDestinationLocation(detectives, destination);
				if (check == 0) {
					continue;
				}
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
					// TODO find out if the player has the required ticket
					//  if it does, construct a SingleMove and add it the collection of moves to return
					ScotlandYard.Ticket rTicket = t.requiredTicket();
					if (player.hasAtLeast(rTicket, 1)) {
						Move.SingleMove moves = new Move.SingleMove(player.piece(), player.location(), t.requiredTicket(), destination);
						set.add(moves);

					}
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)) {
					set.add(new Move.SingleMove(player.piece(), player.location(), ScotlandYard.Ticket.SECRET, destination));
				}

			}

			// TODO return the collection of moves
			return set;
		}

		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

			HashSet<Move.DoubleMove> set2 = new HashSet<>();
			if(player.hasAtLeast(ScotlandYard.Ticket.DOUBLE,1)) {
				for (int destination : setup.graph.adjacentNodes(source)) {
						//TODO check first location
					int check1 = checkDestinationLocation(detectives, destination);
					if(check1 == 0)
						continue;
					for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
						ScotlandYard.Ticket rTicket1 = t.requiredTicket();
						if (player.hasAtLeast(rTicket1, 1) || player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)) {

							for (int finalDestination : setup.graph.adjacentNodes(destination)) {
								for (ScotlandYard.Transport t2 : setup.graph.edgeValueOrDefault(destination, finalDestination, ImmutableSet.of())) {
									int check = checkDestinationLocation(detectives, finalDestination);
									if (check == 0)
										continue;

									ScotlandYard.Ticket rTicket2 = t2.requiredTicket();
									//2 secret
									if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 2)) {
										Move.DoubleMove moves = new Move.DoubleMove(player.piece(), player.location(), ScotlandYard.Ticket.SECRET, destination, ScotlandYard.Ticket.SECRET, finalDestination);
										set2.add(moves);
									}
									//1 rticket1 and 1 secret
									if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1) && player.hasAtLeast(rTicket1, 1)) {
										Move.DoubleMove moves = new Move.DoubleMove(player.piece(), player.location(), rTicket1, destination, ScotlandYard.Ticket.SECRET, finalDestination);
										set2.add(moves);
									}
									//1 secret and 1 rticket2
									if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1) && player.hasAtLeast(rTicket2, 1)) {
										Move.DoubleMove moves = new Move.DoubleMove(player.piece(), player.location(), ScotlandYard.Ticket.SECRET, destination, rTicket2, finalDestination);
										set2.add(moves);
									}
									//rticket1 and rticket2
									if(rTicket1.equals(rTicket2)){
										if (player.hasAtLeast(rTicket1, 2)){
											Move.DoubleMove moves = new Move.DoubleMove(player.piece(), player.location(), rTicket1, destination, rTicket2, finalDestination);
											set2.add(moves);
										}
									} else if (player.hasAtLeast(rTicket1, 1) && player.hasAtLeast(rTicket2, 1)) {
										Move.DoubleMove moves = new Move.DoubleMove(player.piece(), player.location(), rTicket1, destination, rTicket2, finalDestination);
										set2.add(moves);
									}

								}
							}
						}
					}


				}
			}
				return set2;
		}


		private static int checkDestinationLocation(List<Player> detectives, int destination) {
			for (Player detective : detectives) {
				if (detective.location() == destination)
					return 0;
			}

			return 1;
		}
	}
}
