package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAccessorialBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newManualAccessorialBudget("code-727", "label-195")
        assertEquals(ManualAccessorialBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualAccessorialBudget("code-727", "label-195")
        try {
            item.moveTo(ManualAccessorialBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAccessorialBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualAccessorialBudget("code-727", "label-195")
        item.status = ManualAccessorialBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualAccessorialBudgetStatus.PLANNED) }
    }
}
