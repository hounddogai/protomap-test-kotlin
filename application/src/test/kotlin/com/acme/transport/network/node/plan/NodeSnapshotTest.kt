package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeSnapshot("code-185", "label-121")
        assertEquals(NodeSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNodeSnapshot("code-185", "label-121")
        try {
            item.moveTo(NodeSnapshotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeSnapshotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeSnapshot("code-185", "label-121")
        item.status = NodeSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeSnapshotStatus.PLANNED) }
    }
}
