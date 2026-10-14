package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalCoachingBudget("code-142", "label-653")
        assertEquals(SeasonalCoachingBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalCoachingBudget("code-142", "label-653")
        try {
            item.moveTo(SeasonalCoachingBudgetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingBudgetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalCoachingBudget("code-142", "label-653")
        item.status = SeasonalCoachingBudgetStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalCoachingBudgetStatus.NEW) }
    }
}
