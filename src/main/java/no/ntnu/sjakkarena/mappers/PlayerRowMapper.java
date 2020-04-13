package no.ntnu.sjakkarena.mappers;

import no.ntnu.sjakkarena.data.Player;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRowMapper implements RowMapper<Player> {

    @Override
    public Player mapRow(ResultSet rs, int i) throws SQLException {
        Player player = new Player(rs.getInt("player_id"), rs.getString("name"),
                rs.getBoolean("paused"), rs.getFloat("points"), rs.getInt("rounds"),
                rs.getInt("tournament"), rs.getString("icon"),
                rs.getBoolean("in_tournament"), rs.getInt("number_of_white_games"),
                rs.getString("last_played_color"), rs.getInt("same_color_streak"),
                rs.getDouble("bib_number"));
        return player;
    }
}
