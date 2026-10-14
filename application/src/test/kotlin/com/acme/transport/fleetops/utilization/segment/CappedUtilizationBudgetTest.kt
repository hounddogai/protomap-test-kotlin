package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedUtilizationBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedUtilizationBudget("code-75", "name-393")
        assertEquals(CappedUtilizationBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedUtilizationBudget("code-75", "name-393")
        try {
            item.moveTo(CappedUtilizationBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedUtilizationBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedUtilizationBudget("code-75", "name-393")
        item.status = CappedUtilizationBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(CappedUtilizationBudgetStatus.IDLE) }
    }
}
