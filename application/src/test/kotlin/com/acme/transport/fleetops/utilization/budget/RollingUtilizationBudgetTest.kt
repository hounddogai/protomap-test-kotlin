package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingUtilizationBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newRollingUtilizationBudget("code-629", "title-723")
        assertEquals(RollingUtilizationBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingUtilizationBudget("code-629", "title-723")
        try {
            item.moveTo(RollingUtilizationBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingUtilizationBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingUtilizationBudget("code-629", "title-723")
        item.status = RollingUtilizationBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingUtilizationBudgetStatus.OPEN) }
    }
}
