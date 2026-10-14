package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveFreightInvoiceLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        assertEquals(AdaptiveFreightInvoiceLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        try {
            item.moveTo(AdaptiveFreightInvoiceLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveFreightInvoiceLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        item.status = AdaptiveFreightInvoiceLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveFreightInvoiceLedgerStatus.CREATED) }
    }
}
