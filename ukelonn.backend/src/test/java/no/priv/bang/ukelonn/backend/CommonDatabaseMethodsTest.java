/*
 * Copyright 2016-2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.backend;

import static no.priv.bang.ukelonn.backend.CommonDatabaseMethods.*;
import static no.priv.bang.ukelonn.testutils.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import no.priv.bang.ukelonn.UkelonnDatabase;
import no.priv.bang.ukelonn.UkelonnException;
import no.priv.bang.ukelonn.beans.Account;
import no.priv.bang.ukelonn.beans.AdminUser;
import no.priv.bang.ukelonn.beans.Transaction;
import no.priv.bang.ukelonn.beans.TransactionType;
import no.priv.bang.ukelonn.beans.User;

public class CommonDatabaseMethodsTest {

    private static UkelonnServiceProvider provider;

    @BeforeClass
    public static void setupForAllTests() {
        provider = setupFakeOsgiServices();
    }

    @AfterClass
    public static void teardownForAllTests() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        releaseFakeOsgiServices();
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#connectionCheck(Class)}
     * method when no UkelonnDatabase OSGi service has been injected.
     */
    @Test(expected=UkelonnException.class)
    public void testConnectionCheckFailed() {
        // Swap the real derby database with a null
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            provider.setUkelonnDatabase(null);
            UkelonnDatabase database = CommonDatabaseMethods.connectionCheck(getClass(), provider);
            assertNotNull(database); // Will never get here will throw exception on connectionCheck()
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getTransactionTypesFromUkelonnDatabase(Class)}
     * method when a null resultset is returned from the {@link UkelonnDatabase#query(PreparedStatement)}
     * method.
     *
     * Expect no exception to be thrown, and a non-null empty map to be returned.
     */
    @Test()
    public void testGetTransactionTypesFromUkelonnDatabaseNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            provider.setUkelonnDatabase(database);
            Map<Integer, TransactionType> transactiontypes = CommonDatabaseMethods.getTransactionTypesFromUkelonnDatabase(getClass(), provider);
            assertEquals("Expected a non-null, empty map", 0, transactiontypes.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getTransactionTypesFromUkelonnDatabase(Class)}
     * methid when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and a non-null empty map to be returned.
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testGetTransactionTypesFromUkelonnDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            Map<Integer, TransactionType> transactiontypes = CommonDatabaseMethods.getTransactionTypesFromUkelonnDatabase(getClass(), provider);
            assertEquals("Expected a non-null, empty map", 0, transactiontypes.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateBalanseFromDatabase(Class, Account)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and the account balance to be unchanged.
     */
    @Test()
    public void testUpdateBalanseFromDatabaseNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            double originalBalance = 42.5;
            Account account = new Account(1, 1, "jad", "Jane", "Doe", originalBalance);
            CommonDatabaseMethods.updateBalanseFromDatabase(getClass(), provider, account);
            assertEquals("Expected balance to be unchanged", originalBalance, account.getBalance(), 0.0);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateBalanseFromDatabase(Class, Account)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and the account balance to be unchanged.
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testUpdateBalanseFromDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            double originalBalance = 42.5;
            Account account = new Account(1, 1, "jad", "Jane", "Doe", originalBalance);
            CommonDatabaseMethods.updateBalanseFromDatabase(getClass(), provider, account);
            assertEquals("Expected balance to be unchanged", originalBalance, account.getBalance(), 0.0);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#addNewPaymentToAccount(Class, Account, TransactionType, double)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and the method to return {@link CommonDatabaseMethods#UPDATE_FAILED}.
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testAddNewPaymentToAccountWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            double originalBalance = 42.5;
            Account account = new Account(1, 1, "jad", "Jane", "Doe", originalBalance);
            TransactionType jobType = new TransactionType(1, "Støvsuging 1. etasje", 45.0, true, false);
            int updateStatus = CommonDatabaseMethods.addNewPaymentToAccount(getClass(), provider, account, jobType, 45.0);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getAccountInfoFromDatabase(Class, String)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and a dummy {@link Account} object to be returned.
     */
    @Test(expected=UkelonnException.class)
    public void testGetAccountInfoFromDatabaseNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            Account account = CommonDatabaseMethods.getAccountInfoFromDatabase(getClass(), provider, "jad");
            assertNull("Should never get here", account);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateBalanseFromDatabase(Class, Account)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and a dummy {@link Account} object to be returned.
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test(expected=UkelonnException.class)
    public void testGetAccountInfoFromDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            Account account = CommonDatabaseMethods.getAccountInfoFromDatabase(getClass(), provider, "jad");
            assertNull("Should never get here", account);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getAdminUserFromDatabase(Class, String)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and a dummy {@link AdminUser} object to be returned.
     */
    @Test()
    public void testGetAdminUserFromDatabaseNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            AdminUser user = CommonDatabaseMethods.getAdminUserFromDatabase(getClass(), provider, "on");
            assertEquals("Ikke innlogget", user.getFirstname());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateBalanseFromDatabase(Class, Account)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and a dummy {@link AdminUser} object to be returned.
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testGetAdminUserFromDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            AdminUser user = CommonDatabaseMethods.getAdminUserFromDatabase(getClass(), provider, "on");
            assertEquals("Ikke innlogget", user.getFirstname());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getAccounts(Class)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     */
    @Test()
    public void testGetAccountsNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            List<Account> accounts = CommonDatabaseMethods.getAccountsFromDatabase(getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, accounts.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getAccounts(Class)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testGetAccountsWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            List<Account> accounts = CommonDatabaseMethods.getAccountsFromDatabase(getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, accounts.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getPaymentsFromAccount(Account, Class)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     */
    @Test()
    public void testGetPaymentsFromAccountNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            Account account = new Account(1, 1, "jad", "Jane", "Doe", 0.0);
            List<Transaction> payments = CommonDatabaseMethods.getPaymentsFromAccount(account.getAccountId(), getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, payments.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getPaymentsFromAccount(Account, Class)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testGetPaymentsFromAccountWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            Account account = new Account(1, 1, "jad", "Jane", "Doe", 0.0);
            List<Transaction> payments = CommonDatabaseMethods.getPaymentsFromAccount(account.getAccountId(), getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, payments.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getJobsFromAccount(Account, Class)}
     * method when a null resultset is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     */
    @Test()
    public void testGetJobsFromAccountNullResultSet() {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            provider.setUkelonnDatabase(database);
            Account account = new Account(1, 1, "jad", "Jane", "Doe", 0.0);
            List<Transaction> jobs = CommonDatabaseMethods.getJobsFromAccount(account.getAccountId(), getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, jobs.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getJobsFromAccount(Account, Class)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty list to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testGetJobsFromAccountWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            Account account = new Account(1, 1, "jad", "Jane", "Doe", 0.0);
            List<Transaction> jobs = CommonDatabaseMethods.getJobsFromAccount(account.getAccountId(), getClass(), provider);
            assertEquals("Expected a non-null, empty list", 0, jobs.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#registerNewJobInDatabase(Class, Account, int, double)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and an empty map to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testRegisterNewJobInDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            Account account = new Account(1, 1, "jad", "Jane", "Doe", 0.0);
            Map<Integer, TransactionType> transactionTypes = CommonDatabaseMethods.registerNewJobInDatabase(getClass(), provider, account, 1, 45.0, new Date());
            assertEquals("Expected a non-null, empty map", 0, transactionTypes.size());
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#addJobTypeToDatabase(Class, String, double)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testAddJobTypeToDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            int updateStatus = CommonDatabaseMethods.addJobTypeToDatabase(getClass(), provider, "Vaske vindu", 50.0);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateTransactionTypeInDatabase(Class, TransactionType)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testUpdateTransactionTypeInDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            TransactionType transactionType = new TransactionType(1, "Ny jobbtekst", 41.0, true, false);
            int updateStatus = CommonDatabaseMethods.updateTransactionTypeInDatabase(getClass(), provider, transactionType);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#addPaymentTypeToDatabase(Class, String, Double)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testAddPaymentTypeToDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            int updateStatus = CommonDatabaseMethods.addPaymentTypeToDatabase(getClass(), provider, "Vipps", null);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#addUserToDatabase(Class, String, String, String, String, String)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect an {@link UkelonnException} to be thrown
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test(expected=UkelonnException.class)
    public void testAddUserToDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            CommonDatabaseMethods.addUserToDatabase(getClass(), provider, "jdeere", "bamb1", "deere@forest.com", "Julia", "Deere");
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#getUsers(Class)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#query(PreparedStatement)} method.
     *
     * Expect an {@link UkelonnException} to be thrown
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test(expected=UkelonnException.class)
    public void testGetUsersWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            ResultSet resultset = mock(ResultSet.class);
            when(resultset.next()).thenThrow(SQLException.class);
            when(database.query(any(PreparedStatement.class))).thenReturn(resultset);
            provider.setUkelonnDatabase(database);
            List<User> users = CommonDatabaseMethods.getUsers(getClass(), provider);
            assertEquals(0, users.size()); // Will never get here, using the return value so the IDE won't complain
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#changePasswordForUser(String, String, Class)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testChangePasswordForUserWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            int updateStatus = CommonDatabaseMethods.changePasswordForUser("jad", "zecret0", getClass(), provider);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#updateUserInDatabase(Class, User)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testUupdateUserInDatabaseWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            User user = new User(1, "jad", "jane21@gmail.com", "Jane", "Doe");
            int updateStatus = CommonDatabaseMethods.updateUserInDatabase(getClass(), provider, user);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    @Test
    public void testGetAdminUserFromDatabase() {
        AdminUser admin = getAdminUserFromDatabase(getClass(), provider, "on");
        assertEquals("on", admin.getUserName());
        assertEquals(2, admin.getUserId());
        assertEquals(2, admin.getAdministratorId());
        assertEquals("Ola", admin.getFirstname());
        assertEquals("Nordmann", admin.getSurname());

        AdminUser notAdmin = getAdminUserFromDatabase(getClass(), provider, "jad");
        assertEquals("jad", notAdmin.getUserName());
        assertEquals(0, notAdmin.getUserId());
        assertEquals("Ikke innlogget", notAdmin.getFirstname());
        assertNull(notAdmin.getSurname());

        AdminUser notInDabase = getAdminUserFromDatabase(getClass(), provider, "unknownuser");
        assertEquals("unknownuser", notInDabase.getUserName());
        assertEquals(0, notInDabase.getUserId());
        assertEquals("Ikke innlogget", notInDabase.getFirstname());
        assertNull(notInDabase.getSurname());
    }

    @Test
    public void testGetAccountInfoFromDatabase() {
        Account account = getAccountInfoFromDatabase(getClass(), provider, "jad");
        assertEquals("jad", account.getUsername());
        assertEquals(4, account.getUserId());
        assertEquals("Jane", account.getFirstName());
        assertEquals("Doe", account.getLastName());
        List<Transaction> jobs = getJobsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(10, jobs.size());
        List<Transaction> payments = getPaymentsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(10, payments.size());
    }

    /**
     * Corner case test: test what happens when an account has no transactions
     * (the query result is empty)
     */
    @Test(expected=UkelonnException.class)
    public void testGetAccountInfoFromDatabaseAccountHasNoTransactions() {
        Account accountForAdmin = getAccountInfoFromDatabase(getClass(), provider, "on");
        assertNull("Should never get here", accountForAdmin);
    }

    /**
     * Corner case test: test what happens when trying to get an
     * account for a username that isn't present in the database
     */
    @Test(expected=UkelonnException.class)
    public void testGetAccountInfoFromDatabaseWhenAccountDoesNotExist() {
        Account accountNotInDatabase = getAccountInfoFromDatabase(getClass(), provider, "unknownuser");
        assertNull("Should never get here", accountNotInDatabase);
    }

    @Test
    public void testUpdateUserInDatabase() {
        try {
            List<User> users = getUsers(getClass(), provider);
            User jad = findUserInListByName(users, "jad");
            int jadUserid = jad.getUserId();

            String newUsername = "nn";
            String newEmail = "nn213@aol.com";
            String newFirstname = "Nomen";
            String newLastname = "Nescio";

            // Verify that the new values are different from the old values
            assertNotEquals(newUsername, jad.getUsername());
            assertNotEquals(newEmail, jad.getEmail());
            assertNotEquals(newFirstname, jad.getFirstname());
            assertNotEquals(newLastname, jad.getLastname());

            // Create a brand new User bean to use for the update (password won't be used in the update)
            User jadToUpdate = new User(jadUserid, newUsername, newEmail, newFirstname, newLastname);
            int expectedNumberOfUpdatedRecords = 1;
            int numberOfUpdatedRecords = updateUserInDatabase(getClass(), provider, jadToUpdate);
            assertEquals(expectedNumberOfUpdatedRecords, numberOfUpdatedRecords);

            // Read back an updated user and compare with the expected values
            List<User> usersAfterUpdate = getUsers(getClass(), provider);
            assertEquals("Expected no new users added", users.size(), usersAfterUpdate.size());
            User jadAfterUpdate = findUserInListById(usersAfterUpdate, jadUserid);
            assertEquals(newUsername, jadAfterUpdate.getUsername());
            assertEquals(newEmail, jadAfterUpdate.getEmail());
            assertEquals(newFirstname, jadAfterUpdate.getFirstname());
            assertEquals(newLastname, jadAfterUpdate.getLastname());
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testAddJobTypeToDatabase() {
        // Verify precondition
        List<TransactionType> jobTypesBefore = getJobTypesFromTransactionTypes(getTransactionTypesFromUkelonnDatabase(getClass(), provider).values());
        assertEquals(4, jobTypesBefore.size());

        addJobTypeToDatabase(getClass(), provider, "Klippe gress", 45);

        // Verify that a job has been added
        List<TransactionType> jobTypesAfter = getJobTypesFromTransactionTypes(getTransactionTypesFromUkelonnDatabase(getClass(), provider).values());
        assertEquals(5, jobTypesAfter.size());
    }

    @Test
    public void testAddPaymentTypeToDatabase() {
        // Verify precondition
        List<TransactionType> jobTypesBefore = getPaymentTypesFromTransactionTypes(getTransactionTypesFromUkelonnDatabase(getClass(), provider).values());
        assertEquals(2, jobTypesBefore.size());

        addPaymentTypeToDatabase(getClass(), provider, "Sjekk", null);

        // Verify that a job has been added
        List<TransactionType> jobTypesAfter = getPaymentTypesFromTransactionTypes(getTransactionTypesFromUkelonnDatabase(getClass(), provider).values());
        assertEquals(3, jobTypesAfter.size());
    }

    @Test
    public void testAddUserToDatabase() {
        // Verify precondition
        List<User> usersBefore = getUsers(getClass(), provider);
        assertEquals(5, usersBefore.size());

        addUserToDatabase(getClass(), provider, "un", "zecret", "un@gmail.com", "User", "Name");

        // Verify that a user has been added
        List<User> usersAfter = getUsers(getClass(), provider);
        assertEquals(6, usersAfter.size());
    }

    @Test
    public void testUpdateTransactionTypeInDatabase() {
        // Verify the initial state of the transaction type that is to be modified
        Map<Integer, TransactionType> transactionTypes = getTransactionTypesFromUkelonnDatabase(getClass(), provider);
        TransactionType transactionTypeBeforeModification = transactionTypes.get(3);
        assertEquals("Gå med resirk", transactionTypeBeforeModification.getTransactionTypeName());
        assertEquals(Double.valueOf(35), transactionTypeBeforeModification.getTransactionAmount());
        assertTrue(transactionTypeBeforeModification.isTransactionIsWork());
        assertFalse(transactionTypeBeforeModification.isTransactionIsWagePayment());

        // Modify the transaction type
        transactionTypeBeforeModification.setTransactionTypeName("Vaske tøy");
        transactionTypeBeforeModification.setTransactionAmount(75.0);
        updateTransactionTypeInDatabase(getClass(), provider, transactionTypeBeforeModification);

        // Verify the changed state of the transaction type in the database
        transactionTypes = getTransactionTypesFromUkelonnDatabase(getClass(), provider);
        TransactionType transactionTypeAfterModification = transactionTypes.get(3);
        assertEquals("Vaske tøy", transactionTypeAfterModification.getTransactionTypeName());
        assertEquals(Double.valueOf(75), transactionTypeAfterModification.getTransactionAmount());
        assertTrue(transactionTypeAfterModification.isTransactionIsWork());
        assertFalse(transactionTypeAfterModification.isTransactionIsWagePayment());
    }

    @Test
    public void testDeleteTransactions() {
        // Verify initial job size for a user
        Account account = getAccountInfoFromDatabase(getClass(), provider, "jod");
        List<Transaction> initialJobsForJod = getJobsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(2, initialJobsForJod.size());

        // Add two jobs that are to be deleted later
        registerNewJobInDatabase(getClass(), provider, account, 1, 45, new Date());
        registerNewJobInDatabase(getClass(), provider, account, 2, 45, new Date());

        // Verify the number of jobs for the user in the database before deleting any
        List<Transaction> jobs = getJobsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(4, jobs.size());

        // Delete two jobs for the user
        List<Transaction> jobsToDelete = Arrays.asList(jobs.get(0), jobs.get(2));
        deleteTransactions(getClass(), provider, jobsToDelete);

        // Verify that the jobs has been deleted
        List<Transaction> jobsAfterDelete = getJobsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(2, jobsAfterDelete.size());
    }

    @Test
    public void testJoinIds() {
        assertEquals("", CommonDatabaseMethods.joinIds(null).toString());
        assertEquals("", CommonDatabaseMethods.joinIds(Collections.emptyList()).toString());
        Account account = CommonDatabaseMethods.getAccountInfoFromDatabase(getClass(), provider, "jad");
        List<Transaction> jobs = CommonDatabaseMethods.getJobsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals("31, 33, 34, 35, 37, 38, 39, 41, 42, 43", CommonDatabaseMethods.joinIds(jobs).toString());
    }

    @Test
    public void testAddNewPaymentToAccount() {
        // Verify initial number of payments for a user
        Account account = getAccountInfoFromDatabase(getClass(), provider, "jod");
        List<Transaction> initialPaymentsForJod = getPaymentsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(1, initialPaymentsForJod.size());

        // Register a payment
        Map<Integer, TransactionType> transactionTypes = getTransactionTypesFromUkelonnDatabase(getClass(), provider);
        addNewPaymentToAccount(getClass(), provider, account, transactionTypes.get(4), account.getBalance());

        // Verify that a payment have been added
        List<Transaction> paymentsForJod = getPaymentsFromAccount(account.getAccountId(), getClass(), provider);
        assertEquals(2, paymentsForJod.size());
    }

    /**
     * Corner case test: Tests what happens to the {@link CommonDatabaseMethods#addDummyPaymentToAccountSoThatAccountWillAppearInAccountsView(UkelonnDatabase, int)}
     * method when a resultset that throws SQLException is returned from the
     * {@link UkelonnDatabase#update(PreparedStatement)} method.
     *
     * Expect no exception to be thrown, and {@link CommonDatabaseMethods#UPDATE_FAILED} to be returned
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test()
    public void testaddDummyPaymentToAccountSoThatAccountWillAppearInAccountsViewWhenSQLExceptionIsThrown() throws SQLException {
        // Swap the real derby database with a mock
        UkelonnDatabase originalDatabase = provider.getDatabase();
        try {
            UkelonnDatabase database = mock(UkelonnDatabase.class);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(database.prepareStatement(anyString())).thenReturn(statement);
            when(database.update(any(PreparedStatement.class))).thenThrow(SQLException.class);
            provider.setUkelonnDatabase(database);
            int updateStatus = CommonDatabaseMethods.addDummyPaymentToAccountSoThatAccountWillAppearInAccountsView(provider, database, 1);
            assertEquals(CommonDatabaseMethods.UPDATE_FAILED, updateStatus);
        } finally {
            // Restore the real derby database
            provider.setUkelonnDatabase(originalDatabase);
        }
    }

    /**
     * Corner case test for {@link CommonDatabaseMethods#mapUser}
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test(expected=UkelonnException.class)
    public void testMapUserWhenSQLExceptionIsThrown() throws SQLException {
        ResultSet resultset = mock(ResultSet.class);
        when(resultset.getInt(anyString())).thenThrow(SQLException.class);
        User user = CommonDatabaseMethods.mapUser(resultset);
        assertNull(user); // Should never get here because a UkelonnException is thrown
    }

    @Test
    public void testGetResourceAsStringNoResource() {
        String resource = CommonDatabaseMethods.getResourceAsString(provider, "finnesikke");
        assertNull(resource);
    }

    private User findUserInListByName(List<User> users, String username) {
        for (User user : users) {
            if (username.equals(user.getUsername())) {
                return user;
            }
        }

        return null;
    }

    private User findUserInListById(List<User> users, int userId) {
        for (User user : users) {
            if (userId == user.getUserId()) {
                return user;
            }
        }

        return null;
    }

}