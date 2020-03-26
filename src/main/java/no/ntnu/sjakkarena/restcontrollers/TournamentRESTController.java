package no.ntnu.sjakkarena.restcontrollers;

import no.ntnu.sjakkarena.exceptions.NotAbleToUpdateDBException;
import no.ntnu.sjakkarena.exceptions.NotInDatabaseException;
import no.ntnu.sjakkarena.services.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles requests from tournaments
 */
@RestController
@RequestMapping("/tournament")
public class TournamentRESTController {

    @Autowired
    private TournamentService tournamentService;

    /**
     * Get information about the requesting tournament
     *
     * @return information about the requesting tournament
     */
    @RequestMapping(value = "/information", method = RequestMethod.GET)
    public ResponseEntity<String> getTournament() {
        try {
            String responseMessage = tournamentService.getTournament();
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (NotInDatabaseException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the player with the given ID
     *
     * @return 200 OK if successfully deleted, otherwise 400 BAD REQUEST
     */
    @RequestMapping(value = "/delete-player/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deletePlayer(@PathVariable(name = "id") int id,
    @RequestParam(defaultValue = "") String msg) {
        try {
            tournamentService.deletePlayer(id, msg);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotAbleToUpdateDBException e) {
            return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes the player with the given ID
     *
     * @return 200 OK if successfully deleted, otherwise 400 BAD REQUEST
     */
    @RequestMapping(value = "/set-player-inactive/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<String> setPlayerInactive(@PathVariable(name = "id") int playerId,
    @RequestBody String msg) {
        String message = msg.equals("blank") ? "" : msg ;
        try {
            tournamentService.inactivatePlayer(playerId, message);
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
        String responseMessage = tournamentService.getGamesWithPlayerNames();
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/start", method = RequestMethod.PATCH)
    public ResponseEntity<String> startTournament() {
        try {
            tournamentService.startTournament();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch(NotAbleToUpdateDBException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns information about a specific player
     * @return Information about a specific player
     */
    @RequestMapping(value = "/player/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getPlayer(@PathVariable(name = "id") int playerId){
        String player = tournamentService.getPlayer(playerId);
        return new ResponseEntity<>(player, HttpStatus.OK);
    }
}
