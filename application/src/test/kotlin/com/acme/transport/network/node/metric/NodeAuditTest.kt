package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeAudit("code-14", "label-613")
        assertEquals(NodeAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNodeAudit("code-14", "label-613")
        try {
            item.moveTo(NodeAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeAudit("code-14", "label-613")
        item.status = NodeAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeAuditStatus.PLANNED) }
    }
}
