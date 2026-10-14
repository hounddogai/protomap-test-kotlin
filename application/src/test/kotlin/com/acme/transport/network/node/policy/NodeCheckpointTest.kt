package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newNodeCheckpoint("code-447", "name-738")
        assertEquals(NodeCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newNodeCheckpoint("code-447", "name-738")
        try {
            item.moveTo(NodeCheckpointStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeCheckpointStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNodeCheckpoint("code-447", "name-738")
        item.status = NodeCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(NodeCheckpointStatus.CREATED) }
    }
}
