/*
 * Copyright 2018 Steinar Bang
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
package no.priv.bang.ukelonn.api.resources;

import static no.priv.bang.ukelonn.testutils.TestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;

import no.priv.bang.ukelonn.UkelonnService;
import no.priv.bang.ukelonn.api.ServletTestBase;
import no.priv.bang.ukelonn.beans.Account;
import no.priv.bang.ukelonn.beans.Transaction;

public class PaymentsTest extends ServletTestBase {

    @Test
    public void testGetPayments() {
        Account account = getJadAccount();
        Payments resource = new Payments();
        UkelonnService ukelonn = mock(UkelonnService.class);
        when(ukelonn.getPayments(anyInt())).thenReturn(getJadPayments());
        resource.ukelonn = ukelonn;
        List<Transaction> jobs = resource.payments(account.getAccountId());
        assertEquals(10, jobs.size());
    }

}
