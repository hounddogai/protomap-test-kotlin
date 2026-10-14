package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newNodeClaim("code-883", "name-99")
        assertEquals(NodeClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newNodeClaim("code-883", "name-99")
        try {
            item.moveTo(NodeClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNodeClaim("code-883", "name-99")
        item.status = NodeClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(NodeClaimStatus.PLANNED) }
    }
}
