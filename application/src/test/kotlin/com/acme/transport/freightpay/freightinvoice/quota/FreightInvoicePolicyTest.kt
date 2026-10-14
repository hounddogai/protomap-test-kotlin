package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoicePolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoicePolicy("code-517", "label-993")
        assertEquals(FreightInvoicePolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFreightInvoicePolicy("code-517", "label-993")
        try {
            item.moveTo(FreightInvoicePolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoicePolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoicePolicy("code-517", "label-993")
        item.status = FreightInvoicePolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoicePolicyStatus.REQUESTED) }
    }
}
