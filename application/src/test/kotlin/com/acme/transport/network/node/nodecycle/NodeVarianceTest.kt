package com.acme.transport.network.node.nodecycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newNodeVariance("code-333", "name-379")
        assertEquals(NodeVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newNodeVariance("code-333", "name-379")
        try {
            item.moveTo(NodeVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNodeVariance("code-333", "name-379")
        item.status = NodeVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NodeVarianceStatus.PROPOSED) }
    }
}
