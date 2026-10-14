package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceBudget("code-479", "label-129")
        assertEquals(FreightInvoiceBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFreightInvoiceBudget("code-479", "label-129")
        try {
            item.moveTo(FreightInvoiceBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceBudget("code-479", "label-129")
        item.status = FreightInvoiceBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceBudgetStatus.REQUESTED) }
    }
}
