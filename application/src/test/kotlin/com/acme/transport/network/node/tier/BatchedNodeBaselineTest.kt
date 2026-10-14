package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNodeBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedNodeBaseline("code-616", "title-935")
        assertEquals(BatchedNodeBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedNodeBaseline("code-616", "title-935")
        try {
            item.moveTo(BatchedNodeBaselineStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNodeBaselineStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedNodeBaseline("code-616", "title-935")
        item.status = BatchedNodeBaselineStatus.FAILED
        assertFails("move") { item.moveTo(BatchedNodeBaselineStatus.NEW) }
    }
}
