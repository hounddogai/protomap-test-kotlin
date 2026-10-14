package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelSurchargeMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedFuelSurchargeMilestone("code-648", "name-534")
        assertEquals(BatchedFuelSurchargeMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedFuelSurchargeMilestone("code-648", "name-534")
        try {
            item.moveTo(BatchedFuelSurchargeMilestoneStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelSurchargeMilestoneStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedFuelSurchargeMilestone("code-648", "name-534")
        item.status = BatchedFuelSurchargeMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedFuelSurchargeMilestoneStatus.IDLE) }
    }
}
