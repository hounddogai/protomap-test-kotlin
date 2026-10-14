package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodePlanTest {
    @Test
    fun `starts in planned`() {
        val item = newNodePlan("code-51", "name-302")
        assertEquals(NodePlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNodePlan("code-51", "name-302")
        try {
            item.moveTo(NodePlanStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodePlanStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodePlan("code-51", "name-302")
        item.status = NodePlanStatus.CANCELLED
        assertFails("move") { item.moveTo(NodePlanStatus.PLANNED) }
    }
}
