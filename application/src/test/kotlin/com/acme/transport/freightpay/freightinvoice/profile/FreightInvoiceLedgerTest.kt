package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newFreightInvoiceLedger("code-11", "name-727")
        assertEquals(FreightInvoiceLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFreightInvoiceLedger("code-11", "name-727")
        try {
            item.moveTo(FreightInvoiceLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFreightInvoiceLedger("code-11", "name-727")
        item.status = FreightInvoiceLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(FreightInvoiceLedgerStatus.OPEN) }
    }
}
