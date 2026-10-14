package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServicePlanTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedServicePlanTarget("code-591", "name-421")
        assertEquals(BatchedServicePlanTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedServicePlanTarget("code-591", "name-421")
        try {
            item.moveTo(BatchedServicePlanTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServicePlanTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedServicePlanTarget("code-591", "name-421")
        item.status = BatchedServicePlanTargetStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedServicePlanTargetStatus.IDLE) }
    }
}
