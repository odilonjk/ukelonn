package no.priv.bang.ukelonn.bundle.db.liquibase;

import javax.sql.PooledConnection;

import liquibase.Liquibase;
import liquibase.command.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;

public class UkelonnLiquibase {

    public void readSchema(PooledConnection connect) {
        try {
            DatabaseConnection databaseConnection = new JdbcConnection(connect.getConnection());
            Liquibase liquibase = new Liquibase(null, null, databaseConnection);
            Database database = liquibase.getDatabase();
            DiffOutputControl diffOutputControl = new DiffOutputControl();
            CompareControl compareControl = new CompareControl();
            GenerateChangeLogCommand command = new GenerateChangeLogCommand();
            command.setReferenceDatabase(database)
                .setOutputStream(System.out)
                .setCompareControl(compareControl);
            command.setDiffOutputControl(diffOutputControl);

            command.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
