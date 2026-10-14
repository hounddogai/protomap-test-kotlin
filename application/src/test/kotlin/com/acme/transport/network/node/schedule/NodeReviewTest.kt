package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeReviewTest {
    @Test
    fun `starts in new`() {
        val item = newNodeReview("code-918", "label-141")
        assertEquals(NodeReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newNodeReview("code-918", "label-141")
        try {
            item.moveTo(NodeReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNodeReview("code-918", "label-141")
        item.status = NodeReviewStatus.FAILED
        assertFails("move") { item.moveTo(NodeReviewStatus.NEW) }
    }
}
