package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationBudget("code-116", "name-337")
        assertEquals(UtilizationBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newUtilizationBudget("code-116", "name-337")
        try {
            item.moveTo(UtilizationBudgetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationBudgetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationBudget("code-116", "name-337")
        item.status = UtilizationBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationBudgetStatus.PLANNED) }
    }
}
