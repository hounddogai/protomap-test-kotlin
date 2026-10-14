package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodePolicyTest {
    @Test
    fun `starts in new`() {
        val item = newNodePolicy("code-927", "title-559")
        assertEquals(NodePolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newNodePolicy("code-927", "title-559")
        try {
            item.moveTo(NodePolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodePolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNodePolicy("code-927", "title-559")
        item.status = NodePolicyStatus.FAILED
        assertFails("move") { item.moveTo(NodePolicyStatus.NEW) }
    }
}
