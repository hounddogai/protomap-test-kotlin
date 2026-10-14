package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceEntryTest {
    @Test
    fun `starts in open`() {
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        assertEquals(LenientFreightInvoiceEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        try {
            item.moveTo(LenientFreightInvoiceEntryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceEntryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        item.status = LenientFreightInvoiceEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientFreightInvoiceEntryStatus.OPEN) }
    }
}
