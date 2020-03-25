package no.ntnu.sjakkarena.services;

import no.ntnu.sjakkarena.adaptedmonrad.AdaptedMonrad;
import no.ntnu.sjakkarena.adaptedmonrad.AfterTournamentStartAdaptedMonrad;
import no.ntnu.sjakkarena.data.Game;
import no.ntnu.sjakkarena.data.Player;
import no.ntnu.sjakkarena.data.ResultUpdateRequest;
import no.ntnu.sjakkarena.events.GamesCreatedEvent;
import no.ntnu.sjakkarena.events.NewPlayerAddedEvent;
import no.ntnu.sjakkarena.events.TournamentStartedEvent;
import no.ntnu.sjakkarena.exceptions.NotInDatabaseException;
import no.ntnu.sjakkarena.repositories.GameRepository;
import no.ntnu.sjakkarena.repositories.GameWithPlayerNamesRepository;
import no.ntnu.sjakkarena.repositories.PlayerRepository;
import no.ntnu.sjakkarena.repositories.TournamentRepository;
import no.ntnu.sjakkarena.utils.RESTSession;
import no.ntnu.sjakkarena.utils.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService extends UserService {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameWithPlayerNamesRepository gameWithPlayerNamesRepository;

    private AdaptedMonrad adaptedMonrad;

    /**
     * Get games that does not have valid result.
     * @param tournamentID of tournament these games belong to
     * @return List of games with invalid results.
     */
    public List<Game> invalidGames (int tournamentID) {
        List<Game> games = gameRepository.getInvalidResultGames(tournamentID);
        return games;
    }

    // When first player registers a result
    public void awaitResultApproval(ResultUpdateRequest resultUpdateRequest) {
        if (!Validator.pointsIsValid(resultUpdateRequest.getResult())) {
            throw new IllegalArgumentException("Not a valid result");
        }
        // TODO: send event to opponent
    }

    // If second player does not approve game result
    public void resultDisapproved(ResultUpdateRequest resultUpdateRequest) {
        if (!Validator.pointsIsValid(resultUpdateRequest.getResult())) {
            throw new IllegalArgumentException("Not a valid result");
        }
        try {
            Game game = gameRepository.getActiveGame(RESTSession.getUserId(), resultUpdateRequest.getOpponent());
            int gameId = game.getGameId();
            gameRepository.invalidateResult(gameId);
        } catch (NotInDatabaseException e) {
            throw e;
        }
    }

    // If second player approves, do this
    public void addResult(ResultUpdateRequest resultUpdateRequest) {
        if (!Validator.pointsIsValid(resultUpdateRequest.getResult())) {
            throw new IllegalArgumentException("Not a valid result");
        }
        try {
            Game game = gameRepository.getActiveGame(RESTSession.getUserId(), resultUpdateRequest.getOpponent()); // Has requesting user white pieces?
            gameRepository.addResult(game.getGameId(), resultUpdateRequest.getResult());
            onResultAdd();
        } catch (NotInDatabaseException e) {
            throw e;
        }
    }

    private void onResultAdd() {
        int tournamentId = playerRepository.getPlayer(RESTSession.getUserId()).getTournamentId();
        onPlayerListChange(tournamentId);
        this.adaptedMonrad = new AfterTournamentStartAdaptedMonrad();
        manageNewGamesRequest(tournamentId);
    }

    @EventListener
    public void onTournamentStart(TournamentStartedEvent tournamentStartedEvent){
        this.adaptedMonrad = tournamentStartedEvent.getAdaptedMonrad();
        manageNewGamesRequest(tournamentStartedEvent.getTournamentId());
    }

    @EventListener
    public void onNewPlayerAdd(NewPlayerAddedEvent newPlayerAddedEvent){
        if (newPlayerAddedEvent.hasTournamentStarted()) {
            this.adaptedMonrad = newPlayerAddedEvent.getAdaptedMonrad();
            manageNewGamesRequest(newPlayerAddedEvent.getTournamentId());
        }
    }

    private void manageNewGamesRequest(int tournamentId){
        List<Game> newGames = requestNewGames(tournamentId);
        gameRepository.addGames(newGames);
        List<? extends Game> gamesWithPlayerNames =  gameWithPlayerNamesRepository.getActiveGames(tournamentId);
        createAndPublishNewGamesEvent(gamesWithPlayerNames, tournamentId);
    }

    private void createAndPublishNewGamesEvent(List<? extends Game> gameWithPlayerNames, int tournamentId) {
        GamesCreatedEvent gamesCreatedEvent = new GamesCreatedEvent(this, gameWithPlayerNames, tournamentId);
        applicationEventPublisher.publishEvent(gamesCreatedEvent);
    }

    private List<Game> requestNewGames(int tournamentId){
        List<Player> inActivePlayers = playerRepository.getPlayersWhoIsCurrentlyNotPlaying(tournamentId);
        List<Integer> availableTables = tournamentRepository.getAvailableTables(tournamentId);
        return adaptedMonrad.getNewGames(inActivePlayers, availableTables);
    }
}
