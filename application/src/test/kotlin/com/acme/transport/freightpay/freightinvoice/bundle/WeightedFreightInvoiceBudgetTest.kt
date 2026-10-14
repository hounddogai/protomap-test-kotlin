package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFreightInvoiceBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedFreightInvoiceBudget("code-169", "title-633")
        assertEquals(WeightedFreightInvoiceBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedFreightInvoiceBudget("code-169", "title-633")
        try {
            item.moveTo(WeightedFreightInvoiceBudgetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFreightInvoiceBudgetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedFreightInvoiceBudget("code-169", "title-633")
        item.status = WeightedFreightInvoiceBudgetStatus.FAILED
        assertFails("move") { item.moveTo(WeightedFreightInvoiceBudgetStatus.NEW) }
    }
}
