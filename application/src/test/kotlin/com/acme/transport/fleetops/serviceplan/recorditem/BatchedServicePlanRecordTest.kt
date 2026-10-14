package com.acme.transport.fleetops.serviceplan.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServicePlanRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedServicePlanRecord("code-803", "label-810")
        assertEquals(BatchedServicePlanRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBatchedServicePlanRecord("code-803", "label-810")
        try {
            item.moveTo(BatchedServicePlanRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServicePlanRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedServicePlanRecord("code-803", "label-810")
        item.status = BatchedServicePlanRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedServicePlanRecordStatus.PROPOSED) }
    }
}
