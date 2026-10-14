package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCoachingBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultCoachingBudget("code-833", "label-173")
        assertEquals(DefaultCoachingBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultCoachingBudget("code-833", "label-173")
        try {
            item.moveTo(DefaultCoachingBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCoachingBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultCoachingBudget("code-833", "label-173")
        item.status = DefaultCoachingBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultCoachingBudgetStatus.IDLE) }
    }
}
