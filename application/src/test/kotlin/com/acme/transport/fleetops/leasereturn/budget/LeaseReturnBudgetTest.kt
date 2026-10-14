package com.acme.transport.fleetops.leasereturn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnBudgetTest {
    @Test
    fun `starts in new`() {
        val item = newLeaseReturnBudget("code-827", "title-214")
        assertEquals(LeaseReturnBudgetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLeaseReturnBudget("code-827", "title-214")
        try {
            item.moveTo(LeaseReturnBudgetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnBudgetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLeaseReturnBudget("code-827", "title-214")
        item.status = LeaseReturnBudgetStatus.FAILED
        assertFails("move") { item.moveTo(LeaseReturnBudgetStatus.NEW) }
    }
}
