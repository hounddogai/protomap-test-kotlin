package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNodeCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedNodeCheckpoint("code-42", "title-171")
        assertEquals(CappedNodeCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedNodeCheckpoint("code-42", "title-171")
        try {
            item.moveTo(CappedNodeCheckpointStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNodeCheckpointStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedNodeCheckpoint("code-42", "title-171")
        item.status = CappedNodeCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedNodeCheckpointStatus.PROPOSED) }
    }
}
