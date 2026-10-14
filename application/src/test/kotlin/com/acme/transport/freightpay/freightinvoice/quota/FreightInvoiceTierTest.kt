package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceTierTest {
    @Test
    fun `starts in open`() {
        val item = newFreightInvoiceTier("code-149", "name-365")
        assertEquals(FreightInvoiceTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFreightInvoiceTier("code-149", "name-365")
        try {
            item.moveTo(FreightInvoiceTierStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceTierStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFreightInvoiceTier("code-149", "name-365")
        item.status = FreightInvoiceTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(FreightInvoiceTierStatus.OPEN) }
    }
}
