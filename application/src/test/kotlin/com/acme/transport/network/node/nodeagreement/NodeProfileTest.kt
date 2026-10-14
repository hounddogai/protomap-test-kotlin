package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newNodeProfile("code-245", "title-483")
        assertEquals(NodeProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newNodeProfile("code-245", "title-483")
        try {
            item.moveTo(NodeProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newNodeProfile("code-245", "title-483")
        item.status = NodeProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(NodeProfileStatus.REQUESTED) }
    }
}
