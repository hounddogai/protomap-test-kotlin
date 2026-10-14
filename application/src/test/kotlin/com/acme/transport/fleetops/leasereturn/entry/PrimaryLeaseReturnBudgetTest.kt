package com.acme.transport.fleetops.leasereturn.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLeaseReturnBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryLeaseReturnBudget("code-597", "label-197")
        assertEquals(PrimaryLeaseReturnBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryLeaseReturnBudget("code-597", "label-197")
        try {
            item.moveTo(PrimaryLeaseReturnBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLeaseReturnBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryLeaseReturnBudget("code-597", "label-197")
        item.status = PrimaryLeaseReturnBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryLeaseReturnBudgetStatus.IDLE) }
    }
}
