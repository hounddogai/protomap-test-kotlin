package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeSegmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newNodeSegment("code-538", "label-294")
        assertEquals(NodeSegmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newNodeSegment("code-538", "label-294")
        try {
            item.moveTo(NodeSegmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeSegmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNodeSegment("code-538", "label-294")
        item.status = NodeSegmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NodeSegmentStatus.PROPOSED) }
    }
}
