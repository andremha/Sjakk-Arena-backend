package no.ntnu.sjakkarena.repositories;

import no.ntnu.sjakkarena.data.Player;
import no.ntnu.sjakkarena.exceptions.NotAbleToUpdateDBException;
import no.ntnu.sjakkarena.mappers.PlayerRowMapper;
import no.ntnu.sjakkarena.utils.DBInteractionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
public class PlayerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Player> rowMapper = new PlayerRowMapper();

    /**
     * Adds a new player to the database
     * @param player the player to be added
     * @return the autogenerated player id
     */
    public int addNewPlayer(Player player){
        String values = DBInteractionHelper.toValuesString(player.getName(),
                player.getTournament(), player.getIcon());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            //code from https://stackoverflow.com/questions/12882874/how-can-i-get-the-autoincremented-id-when-i-insert-a-record-in-a-table-via-jdbct
            jdbcTemplate.update(
                    new PreparedStatementCreator() {
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps =
                                    connection.prepareStatement("INSERT INTO sjakkarena.player (`name`, `tournament`, `icon`)" +
                                            " VALUES " + values, new String[] {"id"});
                            return ps;
                        }
                    },
                    keyHolder);
            return keyHolder.getKey().intValue();
        }
        catch(DataAccessException e){
            throw new NotAbleToUpdateDBException("Could not add user to database. Possible reasons: \n" +
                    "1. User is already registered in database \n" +
                    "2. Name/value pairs in JSON are missing");
        }
    }

    /**
     * Returns the name of all the players enrolled in the given tournament
     * @param tournamentId the id of the tournament where the players are enrolled
     * @return
     */
    public Collection<Player> getAllPlayerNamesInTournament(int tournamentId) {
        List<Player> names =  jdbcTemplate.query("SELECT `player_id`, `name`, `icon` FROM  sjakkarena.player WHERE " +
                "tournament = " + tournamentId, rowMapper);
        return names;
    }

    /**
     * Deletes a player from the database
     * @param id
     */
    public void deletePlayer(int id) {
        try {
            jdbcTemplate.update("DELETE FROM sjakkarena.player WHERE player_id = " + id);
        }
        catch(DataAccessException e){
            throw new NotAbleToUpdateDBException("Couldn't delete player from database");
        }
    }

    /**
     * Set player field 'active' to 0.
     * @param id for the player to change value for.
     */
    public void setPlayerInactive(int id) {
        String updateQuery = "UPDATE sjakkarena.player SET active = 1 WHERE player_id = ?";
        try {
            jdbcTemplate.update(updateQuery, id);
        }
        catch(DataAccessException e){
            throw new NotAbleToUpdateDBException("Could not set active filed to 0");
        }
    }
}
