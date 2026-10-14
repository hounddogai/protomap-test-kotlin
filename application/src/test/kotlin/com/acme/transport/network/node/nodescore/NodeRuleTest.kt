package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeRuleTest {
    @Test
    fun `starts in created`() {
        val item = newNodeRule("code-887", "label-22")
        assertEquals(NodeRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newNodeRule("code-887", "label-22")
        try {
            item.moveTo(NodeRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newNodeRule("code-887", "label-22")
        item.status = NodeRuleStatus.RETIRED
        assertFails("move") { item.moveTo(NodeRuleStatus.CREATED) }
    }
}
