package no.ntnu.sjakkarena.controllers.RestControllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.ntnu.sjakkarena.DBChangeNotifier;
import no.ntnu.sjakkarena.utils.Session;
import no.ntnu.sjakkarena.data.GameTableElement;
import no.ntnu.sjakkarena.data.Player;
import no.ntnu.sjakkarena.data.Tournament;
import no.ntnu.sjakkarena.exceptions.NotAbleToUpdateDBException;
import no.ntnu.sjakkarena.repositories.GameRepository;
import no.ntnu.sjakkarena.repositories.PlayerRepository;
import no.ntnu.sjakkarena.repositories.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * Handles requests from tournaments
 */
@RestController
@RequestMapping("/tournament")
public class TournamentRESTController {

    @Autowired
    private DBChangeNotifier dbChangeNotifier;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    /**
     * Get information about the requesting tournament
     *
     * @return information about the requesting tournament
     */
    @RequestMapping(value = "/information", method = RequestMethod.GET)
    public ResponseEntity<String> getTournament() {
        int tournamentId = Session.getUserId();
        Tournament tournament = tournamentRepository.getTournament(tournamentId);
        Gson gson = new Gson();
        return new ResponseEntity<>(gson.toJson(tournament), HttpStatus.OK);
    }

    /**
     * Deletes the player with the given ID
     *
     * @return 200 OK if successfully deleted, otherwise 400 BAD REQUEST
     */
    @RequestMapping(value = "/delete-player/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deletePlayer(@PathVariable(name = "id") int id) {
        try {
            playerRepository.deletePlayer(id);
            dbChangeNotifier.notifyUpdatedPlayerList(Session.getUserId());
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotAbleToUpdateDBException e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns the games of the requesting tournament
     *
     * @return the games of the requesting tournament
     */
    @RequestMapping(value = "/games", method = RequestMethod.GET)
    public ResponseEntity<String> getGames() {
        int tournamentId = Session.getUserId();
        Collection<GameTableElement> games = gameRepository.getGames(tournamentId);
        Gson gson = new GsonBuilder().serializeNulls().create();
        return new ResponseEntity<>(gson.toJson(games), HttpStatus.OK);
    }
}
