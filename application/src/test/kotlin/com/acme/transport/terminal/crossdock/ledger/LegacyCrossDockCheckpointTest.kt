package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyCrossDockCheckpoint("code-814", "label-432")
        assertEquals(LegacyCrossDockCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLegacyCrossDockCheckpoint("code-814", "label-432")
        try {
            item.moveTo(LegacyCrossDockCheckpointStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockCheckpointStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyCrossDockCheckpoint("code-814", "label-432")
        item.status = LegacyCrossDockCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyCrossDockCheckpointStatus.DRAFT) }
    }
}
