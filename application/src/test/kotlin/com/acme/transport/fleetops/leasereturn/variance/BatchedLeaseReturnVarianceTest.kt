package com.acme.transport.fleetops.leasereturn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLeaseReturnVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedLeaseReturnVariance("code-707", "label-704")
        assertEquals(BatchedLeaseReturnVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedLeaseReturnVariance("code-707", "label-704")
        try {
            item.moveTo(BatchedLeaseReturnVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLeaseReturnVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedLeaseReturnVariance("code-707", "label-704")
        item.status = BatchedLeaseReturnVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedLeaseReturnVarianceStatus.IDLE) }
    }
}
