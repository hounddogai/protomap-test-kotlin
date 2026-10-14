package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBrokerCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingBrokerCheckpoint("code-961", "name-940")
        assertEquals(PendingBrokerCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingBrokerCheckpoint("code-961", "name-940")
        try {
            item.moveTo(PendingBrokerCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBrokerCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingBrokerCheckpoint("code-961", "name-940")
        item.status = PendingBrokerCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingBrokerCheckpointStatus.PROPOSED) }
    }
}
