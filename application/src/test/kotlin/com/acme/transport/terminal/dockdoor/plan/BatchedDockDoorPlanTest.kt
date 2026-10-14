package com.acme.transport.terminal.dockdoor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDockDoorPlanTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedDockDoorPlan("code-855", "title-445")
        assertEquals(BatchedDockDoorPlanStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBatchedDockDoorPlan("code-855", "title-445")
        try {
            item.moveTo(BatchedDockDoorPlanStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDockDoorPlanStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedDockDoorPlan("code-855", "title-445")
        item.status = BatchedDockDoorPlanStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedDockDoorPlanStatus.REQUESTED) }
    }
}
