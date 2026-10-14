package com.acme.transport.network.node.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodeRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedNodeRequest("code-665", "name-447")
        assertEquals(BatchedNodeRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedNodeRequest("code-665", "name-447")
        try {
            item.moveTo(BatchedNodeRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodeRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedNodeRequest("code-665", "name-447")
        item.status = BatchedNodeRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedNodeRequestStatus.PROPOSED) }
    }
}
