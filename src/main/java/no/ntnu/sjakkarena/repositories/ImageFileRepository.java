package no.ntnu.sjakkarena.repositories;

import no.ntnu.sjakkarena.data.Image;
import no.ntnu.sjakkarena.exceptions.TroubleUpdatingDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// code adapted from from: https://www.callicoder.com/spring-boot-file-upload-download-jpa-hibernate-mysql-database-example/

@Repository
public interface ImageFileRepository extends JpaRepository<Image, String> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addNewImage(String fileName, int playerId, int gameId, long imageSize) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            //Adapted code from https://stackoverflow.com/questions/12882874/how-can-i-get-the-autoincremented-id-when-i-insert-a-record-in-a-table-via-jdbct
            jdbcTemplate.update(
                    new PreparedStatementCreator() {
                        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                            PreparedStatement ps =
                                    connection.prepareStatement("INSERT INTO sjakkarena.image (`player`.`id`, " +
                                            "`game`.`Id`, `image`.`size`, `image`.`timeUploaded`) " +
                                            "VALUES (\"" + playerId + "\", " + gameId + ", \"" +
                                            imageSize + "\", + localDateTime.now() )", new String[]{"id"});
                            return ps;
                        }
                    },
                    keyHolder);
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            throw new TroubleUpdatingDBException("Could not add image to database. Possible reasons: \n" +
                    "1. Image is already registered in database \n" +
                    "2. Name/value pairs in JSON are missing");
        }
    }

}