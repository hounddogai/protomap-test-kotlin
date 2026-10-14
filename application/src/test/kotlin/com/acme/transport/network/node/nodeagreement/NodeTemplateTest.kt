package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeTemplate("code-665", "name-948")
        assertEquals(NodeTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNodeTemplate("code-665", "name-948")
        try {
            item.moveTo(NodeTemplateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeTemplateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeTemplate("code-665", "name-948")
        item.status = NodeTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeTemplateStatus.PLANNED) }
    }
}
