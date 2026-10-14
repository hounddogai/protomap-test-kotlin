package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeBundle("code-264", "label-451")
        assertEquals(NodeBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNodeBundle("code-264", "label-451")
        try {
            item.moveTo(NodeBundleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeBundleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeBundle("code-264", "label-451")
        item.status = NodeBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeBundleStatus.PLANNED) }
    }
}
