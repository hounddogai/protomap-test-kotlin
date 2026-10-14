package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServicePlanBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledServicePlanBudget("code-710", "name-700")
        assertEquals(ScheduledServicePlanBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledServicePlanBudget("code-710", "name-700")
        try {
            item.moveTo(ScheduledServicePlanBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServicePlanBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledServicePlanBudget("code-710", "name-700")
        item.status = ScheduledServicePlanBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledServicePlanBudgetStatus.OPEN) }
    }
}
