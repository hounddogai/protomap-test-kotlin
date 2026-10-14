package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNodeGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedNodeGroup("code-206", "label-153")
        assertEquals(CappedNodeGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedNodeGroup("code-206", "label-153")
        try {
            item.moveTo(CappedNodeGroupStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNodeGroupStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedNodeGroup("code-206", "label-153")
        item.status = CappedNodeGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedNodeGroupStatus.PLANNED) }
    }
}
