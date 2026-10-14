package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBrokerCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedBrokerCheckpoint("code-46", "name-327")
        assertEquals(CappedBrokerCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedBrokerCheckpoint("code-46", "name-327")
        try {
            item.moveTo(CappedBrokerCheckpointStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBrokerCheckpointStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedBrokerCheckpoint("code-46", "name-327")
        item.status = CappedBrokerCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(CappedBrokerCheckpointStatus.DRAFT) }
    }
}
