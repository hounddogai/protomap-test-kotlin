package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingNodeRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingNodeRollup("code-381", "label-117")
        assertEquals(PendingNodeRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingNodeRollup("code-381", "label-117")
        try {
            item.moveTo(PendingNodeRollupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingNodeRollupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingNodeRollup("code-381", "label-117")
        item.status = PendingNodeRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingNodeRollupStatus.PROPOSED) }
    }
}
