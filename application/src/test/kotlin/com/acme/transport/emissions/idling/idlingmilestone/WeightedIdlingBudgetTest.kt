package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedIdlingBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedIdlingBudget("code-248", "label-20")
        assertEquals(WeightedIdlingBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedIdlingBudget("code-248", "label-20")
        try {
            item.moveTo(WeightedIdlingBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedIdlingBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedIdlingBudget("code-248", "label-20")
        item.status = WeightedIdlingBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedIdlingBudgetStatus.CREATED) }
    }
}
