package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFreightInvoiceBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newPendingFreightInvoiceBudget("code-286", "title-838")
        assertEquals(PendingFreightInvoiceBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingFreightInvoiceBudget("code-286", "title-838")
        try {
            item.moveTo(PendingFreightInvoiceBudgetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFreightInvoiceBudgetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingFreightInvoiceBudget("code-286", "title-838")
        item.status = PendingFreightInvoiceBudgetStatus.FAILED
        assertFails("move") { item.moveTo(PendingFreightInvoiceBudgetStatus.NEW) }
    }
}
