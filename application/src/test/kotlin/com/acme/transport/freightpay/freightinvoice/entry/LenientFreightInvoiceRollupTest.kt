package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceRollupTest {
    @Test
    fun `starts in open`() {
        val item = newLenientFreightInvoiceRollup("code-454", "label-126")
        assertEquals(LenientFreightInvoiceRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLenientFreightInvoiceRollup("code-454", "label-126")
        try {
            item.moveTo(LenientFreightInvoiceRollupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceRollupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientFreightInvoiceRollup("code-454", "label-126")
        item.status = LenientFreightInvoiceRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientFreightInvoiceRollupStatus.OPEN) }
    }
}
