package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServicePlanBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        assertEquals(SeasonalServicePlanBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        try {
            item.moveTo(SeasonalServicePlanBudgetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServicePlanBudgetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalServicePlanBudget("code-983", "name-49")
        item.status = SeasonalServicePlanBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalServicePlanBudgetStatus.REQUESTED) }
    }
}
