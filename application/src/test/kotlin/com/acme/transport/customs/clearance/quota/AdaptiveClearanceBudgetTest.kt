package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveClearanceBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        assertEquals(AdaptiveClearanceBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        try {
            item.moveTo(AdaptiveClearanceBudgetStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveClearanceBudgetStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        item.status = AdaptiveClearanceBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveClearanceBudgetStatus.PLANNED) }
    }
}
