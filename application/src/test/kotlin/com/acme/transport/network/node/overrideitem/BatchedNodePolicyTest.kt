package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodePolicyTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedNodePolicy("code-311", "label-751")
        assertEquals(BatchedNodePolicyStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedNodePolicy("code-311", "label-751")
        try {
            item.moveTo(BatchedNodePolicyStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodePolicyStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedNodePolicy("code-311", "label-751")
        item.status = BatchedNodePolicyStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedNodePolicyStatus.PLANNED) }
    }
}
