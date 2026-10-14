package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeScore("code-929", "name-249")
        assertEquals(NodeScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNodeScore("code-929", "name-249")
        try {
            item.moveTo(NodeScoreStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeScoreStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeScore("code-929", "name-249")
        item.status = NodeScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeScoreStatus.PLANNED) }
    }
}
