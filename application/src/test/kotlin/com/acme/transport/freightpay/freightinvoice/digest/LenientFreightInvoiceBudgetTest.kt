package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newLenientFreightInvoiceBudget("code-632", "name-950")
        assertEquals(LenientFreightInvoiceBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientFreightInvoiceBudget("code-632", "name-950")
        try {
            item.moveTo(LenientFreightInvoiceBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientFreightInvoiceBudget("code-632", "name-950")
        item.status = LenientFreightInvoiceBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientFreightInvoiceBudgetStatus.OPEN) }
    }
}
