package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLeaseReturnRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        assertEquals(BatchedLeaseReturnRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        try {
            item.moveTo(BatchedLeaseReturnRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLeaseReturnRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        item.status = BatchedLeaseReturnRuleStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedLeaseReturnRuleStatus.IDLE) }
    }
}
