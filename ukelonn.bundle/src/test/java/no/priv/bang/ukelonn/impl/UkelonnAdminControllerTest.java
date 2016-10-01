package no.priv.bang.ukelonn.impl;

import static no.priv.bang.ukelonn.testutils.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import static no.priv.bang.ukelonn.impl.CommonDatabaseMethods.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;

public class UkelonnAdminControllerTest {

    @BeforeClass
    public static void setupForAllTests() {
        setupFakeOsgiServices();
    }

    @AfterClass
    public static void teardownForAllTests() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	releaseFakeOsgiServices();
    }

    @Test
    public void testNoAdministratorUsername() {
        UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

        assertNull(ukelonnAdmin.getAdministratorUsername());
        assertEquals(0, ukelonnAdmin.getAdministratorUserId());
        assertEquals(0, ukelonnAdmin.getAdministratorId());
        assertEquals("Ikke innlogget", ukelonnAdmin.getAdministratorFornavn());
        assertEquals("", ukelonnAdmin.getAdministratorEtternavn());
    }

    @Test
    public void testUserNotAdministrator() {
        UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

        // Set non-administrator user
        ukelonnAdmin.setAdministratorUsername("jad");

        assertEquals("jad", ukelonnAdmin.getAdministratorUsername());
        assertEquals(0, ukelonnAdmin.getAdministratorUserId());
        assertEquals(0, ukelonnAdmin.getAdministratorId());
        assertEquals("Ikke innlogget", ukelonnAdmin.getAdministratorFornavn());
        assertEquals("", ukelonnAdmin.getAdministratorEtternavn());
    }

    @Test
    public void testUserIsAdministrator() {
    	try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            assertEquals("on", ukelonnAdmin.getAdministratorUsername());
            assertEquals(1, ukelonnAdmin.getAdministratorUserId());
            assertEquals(1, ukelonnAdmin.getAdministratorId());
            assertEquals("Ola", ukelonnAdmin.getAdministratorFornavn());
            assertEquals("Nordmann", ukelonnAdmin.getAdministratorEtternavn());

            // Check the initial empty values for the account that is to be administrated
            assertNull(ukelonnAdmin.getAccount());
            assertEquals(0.0, ukelonnAdmin.getBalanse(), .1);
            assertEquals(0, ukelonnAdmin.getJobs().size());
            assertEquals(0, ukelonnAdmin.getPayments().size());
            assertEquals(2, ukelonnAdmin.getPaymentTypes().size());
            assertNull(ukelonnAdmin.getNewPaymentType());
            assertEquals(0.0, ukelonnAdmin.getNewPayment(), 0.1);

            // Test selecting an account and check that
            // the values become non-empty
            Account selectedAccount = ukelonnAdmin.getAccounts().get(0);
            ukelonnAdmin.setAccount(selectedAccount);
            assertEquals(673.0, ukelonnAdmin.getBalanse(), .1);
            assertEquals(10, ukelonnAdmin.getJobs().size());
            assertEquals(10, ukelonnAdmin.getPayments().size());
            assertEquals(2, ukelonnAdmin.getPaymentTypes().size());
            TransactionType payToBank = ukelonnAdmin.findPayToBank(ukelonnAdmin.getPaymentTypes());
            assertEquals(payToBank, ukelonnAdmin.getNewPaymentType());
            assertEquals(673.0, ukelonnAdmin.getNewPayment(), 0.1);
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testRegisterNewPayment() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Test selecting an account and verify expected initial state
            Account selectedAccount = ukelonnAdmin.getAccounts().get(0);
            ukelonnAdmin.setAccount(selectedAccount);
            double initialBalance = 673.0;
            assertEquals(initialBalance, ukelonnAdmin.getBalanse(), .1);
            assertEquals(10, ukelonnAdmin.getJobs().size());
            int initialNumberOfPayments = 10;
            assertEquals(initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(2, ukelonnAdmin.getPaymentTypes().size());

            // Check that the default payment type is pay to bank
            TransactionType payToBank = ukelonnAdmin.findPayToBank(ukelonnAdmin.getPaymentTypes());
            assertEquals(payToBank, ukelonnAdmin.getNewPaymentType());
            assertEquals(673.0, ukelonnAdmin.getNewPayment(), 0.1);

            // Simulate the button click
            ActionEvent event = mock(ActionEvent.class);
            ukelonnAdmin.registerNewPayment(event);

            // Verify that a payment has been made and that balance has been set to 0.0
            assertEquals(initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(0.0, ukelonnAdmin.getBalanse(), .1);

            // Register a payment with a payment type but without an amount
            TransactionType paymentType = ukelonnAdmin.getPaymentTypes().get(0);
            ukelonnAdmin.setNewPaymentType(paymentType);
            ukelonnAdmin.registerNewPayment(event);

            // Verify that no new payment has been made (payment and balance unchanged)
            assertEquals(initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(0.0, ukelonnAdmin.getBalanse(), .1);

            // Set an payment type and an amount
            ukelonnAdmin.setNewPaymentType(paymentType);
            double newAmount = 35.0;
            ukelonnAdmin.setNewPayment(newAmount);
            ukelonnAdmin.registerNewPayment(event);

            // Verify updated values
            assertEquals(initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(0.0 - newAmount, ukelonnAdmin.getBalanse(), .1);

            // Verify that the new payment type and amount have been nulled
            assertNull(ukelonnAdmin.getNewPaymentType());
            assertEquals(0.0, ukelonnAdmin.getNewPayment(), 0.1);
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testRegisterNewNegativePayment() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Test selecting an account and save initial state
            Account selectedAccount = ukelonnAdmin.getAccounts().get(0);
            ukelonnAdmin.setAccount(selectedAccount);
            int initialNumberOfPayments = ukelonnAdmin.getPayments().size();
            double initialBalance = ukelonnAdmin.getBalanse();

            // Register a payment with a negative amount value
            ActionEvent event = mock(ActionEvent.class);
            TransactionType paymentType = ukelonnAdmin.getPaymentTypes().get(0);
            ukelonnAdmin.setNewPaymentType(paymentType);
            double newAmount = -35.0;
            ukelonnAdmin.setNewPayment(newAmount);
            ukelonnAdmin.registerNewPayment(event);

            // Verify that setting a negative amount has the same effect as a positive amount
            assertEquals("Expected only 10 returned values", initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(initialBalance + newAmount, ukelonnAdmin.getBalanse(), .1);
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testRegisterNewPaymentWithNoAccountSet() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Note expected initial state
            int initialNumberOfPayments = ukelonnAdmin.getPayments().size();
            double initialBalance = ukelonnAdmin.getBalanse();

            // Register a payment
            ActionEvent event = mock(ActionEvent.class);
            TransactionType paymentType = ukelonnAdmin.getPaymentTypes().get(0);
            ukelonnAdmin.setNewPaymentType(paymentType);
            double newAmount = 35.0;
            ukelonnAdmin.setNewPayment(newAmount);
            ukelonnAdmin.registerNewPayment(event);

            // Verify that setting had no effect
            assertEquals(initialNumberOfPayments, ukelonnAdmin.getPayments().size());
            assertEquals(initialBalance, ukelonnAdmin.getBalanse(), .1);
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testCreateNewJobType() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Verify expected initial state
            assertNull(ukelonnAdmin.getNewJobTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewJobTypeAmount(), 0.1);
            int initialNumberOfJobTypes = 3;
            assertEquals(initialNumberOfJobTypes, ukelonnAdmin.getJobTypes().size());

            // Try registering an new job type without a name and an amount (should fail)
            ActionEvent event = mock(ActionEvent.class);
            ukelonnAdmin.registerNewJobType(event);

            // Verify that no new job type has been created
            assertEquals(initialNumberOfJobTypes, ukelonnAdmin.getJobTypes().size());

            // Try registering a new job with a name, without an amount (should fail)
            String newJobTypeName = "Rydde på kjøkkenet";
            ukelonnAdmin.setNewJobTypeName(newJobTypeName);
            ukelonnAdmin.registerNewJobType(event);

            // Verify that no new JobType has been created
            assertEquals(initialNumberOfJobTypes, ukelonnAdmin.getJobTypes().size());

            // Verify that the name hasn't been reset
            assertEquals(newJobTypeName, ukelonnAdmin.getNewJobTypeName());

            // Corner case: Set a negative amount and verify that no new job type is created
            ukelonnAdmin.setNewJobTypeAmount(-1.0);
            ukelonnAdmin.registerNewJobType(event);

            // Verify that no new JobType has been created
            assertEquals(initialNumberOfJobTypes, ukelonnAdmin.getJobTypes().size());

            // Verify that the name hasn't been reset
            assertEquals(newJobTypeName, ukelonnAdmin.getNewJobTypeName());

            // Verify that the negative amount hasn't been touched
            assertEquals(-1.0, ukelonnAdmin.getNewJobTypeAmount(), 0.1);

            // Regular case: has name and amount -> create a new payment type
            ukelonnAdmin.setNewJobTypeAmount(15);
            ukelonnAdmin.registerNewJobType(event);

            // Verify that a new JobType has been created
            assertEquals(initialNumberOfJobTypes + 1, ukelonnAdmin.getJobTypes().size());

            // Verify that the new type name and amount has been cleared
            assertNull(ukelonnAdmin.getNewJobTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewJobTypeAmount(), 0.1);

            // Finally just checking that having no name for a new job type is
            // enough to stop it from being created.
            ukelonnAdmin.setNewJobTypeAmount(11.0);
            ukelonnAdmin.registerNewJobType(event);

            // Verify that no new JobType has been created
            assertEquals(initialNumberOfJobTypes + 1, ukelonnAdmin.getJobTypes().size());

            // Verify that the amount wasn't blanked
            assertEquals(11.0, ukelonnAdmin.getNewJobTypeAmount(), 0.1);
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testEditJobType() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Verify expected initial state
            assertNull(ukelonnAdmin.getNewJobTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewJobTypeAmount(), 0.1);
            int initialNumberOfJobTypes = 3;
            List<TransactionType> jobTypes = ukelonnAdmin.getJobTypes();
            assertEquals(initialNumberOfJobTypes, jobTypes.size());

            // Mock an edit event changing the jobType name
            // and call the function listening to cell edit events
            TransactionType jobType = jobTypes.get(0);
            Integer editedJobTypeId = jobType.getId();
            DataTable mockedDataTable = mock(DataTable.class);
            when(mockedDataTable.getRowData()).thenReturn(ukelonnAdmin.getJobTypes().get(0));
            UIColumn column = mock(UIColumn.class);
            when(column.getHeaderText()).thenReturn("Navn");
            CellEditEvent event = mock(CellEditEvent.class);
            when(event.getComponent()).thenReturn(mockedDataTable);
            when(event.getColumn()).thenReturn(column);
            String oldValue = jobType.getTransactionTypeName();
            when(event.getOldValue()).thenReturn(oldValue);
            String newValue = "New value";
            when(event.getNewValue()).thenReturn(newValue);
            jobType.setTransactionTypeName(newValue);
            ukelonnAdmin.onJobTypeCellEdit(event);

            // Verify that the jobType read back from the database
            // has the modified value for the jobType name
            Map<Integer, TransactionType> transactionTypes = getTransactionTypesFromUkelonnDatabase(getClass());
            TransactionType jobTypeFromDatabase = transactionTypes.get(editedJobTypeId);
            assertEquals(newValue, jobTypeFromDatabase.getTransactionTypeName());
        } finally {
            restoreTestDatabase();
        }
    }

    @Test
    public void testCreateNewPaymentType() {
        try {
            UkelonnAdminController ukelonnAdmin = new UkelonnAdminController();

            // Set administrator user
            ukelonnAdmin.setAdministratorUsername("on");

            // Verify expected initial state
            assertNull(ukelonnAdmin.getNewPaymentTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewPaymentTypeAmount(), 0.1);
            int initialNumberOfPaymentTypes = 2;
            assertEquals(initialNumberOfPaymentTypes, ukelonnAdmin.getPaymentTypes().size());

            // Try registering an new payment type without a name and an amount (should fail)
            ActionEvent event = mock(ActionEvent.class);
            ukelonnAdmin.registerNewPaymentType(event);

            // Verify that no new payment type has been created
            assertEquals(initialNumberOfPaymentTypes, ukelonnAdmin.getPaymentTypes().size());

            // Try registering a new payment type with an empty string name, should not work
            ukelonnAdmin.setNewPaymentTypeName("");
            ukelonnAdmin.registerNewPaymentType(event);

            // Verify that no new payment type has been created
            assertEquals(initialNumberOfPaymentTypes, ukelonnAdmin.getPaymentTypes().size());

            // Try registering a new payment type with a name, without an amount (should work)
            String newJobTypeName = "Kontanter";
            ukelonnAdmin.setNewPaymentTypeName(newJobTypeName);
            ukelonnAdmin.registerNewPaymentType(event);

            // Verify that a new payment type has been created
            assertEquals(initialNumberOfPaymentTypes + 1, ukelonnAdmin.getPaymentTypes().size());

            // Verify that the new payment values have been blanked after creating the new payment type
            assertNull(ukelonnAdmin.getNewPaymentTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewPaymentTypeAmount(), 0.1);

            // Try registering a new payment type with a name, and an amount (should also work)
            String anotherNewJobTypeName = "Påfyll reisekort";
            ukelonnAdmin.setNewPaymentTypeName(anotherNewJobTypeName);
            double amountForAnotherNewJobTypeName = 100.0;
            ukelonnAdmin.setNewPaymentTypeAmount(amountForAnotherNewJobTypeName);
            ukelonnAdmin.registerNewPaymentType(event);

            // Verify that another new payment type has been created
            assertEquals(initialNumberOfPaymentTypes + 2, ukelonnAdmin.getPaymentTypes().size());

            // Verify that the last payment type with th new name has the expected amount
            TransactionType lastPaymentType = findTransactionTypeWithName(ukelonnAdmin.getPaymentTypes(), anotherNewJobTypeName);
            assertEquals(amountForAnotherNewJobTypeName, lastPaymentType.getTransactionAmount(), 0.1);

            // Verify that the new payment values have been blanked after creating the new payment type
            assertNull(ukelonnAdmin.getNewPaymentTypeName());
            assertEquals(0.0, ukelonnAdmin.getNewPaymentTypeAmount(), 0.1);
        } finally {
            restoreTestDatabase();
        }
    }

    private TransactionType findTransactionTypeWithName(ArrayList<TransactionType> transactionTypes, String name) {
        for (TransactionType transactionType : transactionTypes) {
            if (name.equals(transactionType.getTransactionTypeName())) {
                return transactionType;
            }
        }

        return null;
    }

}
