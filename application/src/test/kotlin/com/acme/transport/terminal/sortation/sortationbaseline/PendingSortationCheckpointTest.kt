package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingSortationCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingSortationCheckpoint("code-413", "label-365")
        assertEquals(PendingSortationCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingSortationCheckpoint("code-413", "label-365")
        try {
            item.moveTo(PendingSortationCheckpointStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingSortationCheckpointStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingSortationCheckpoint("code-413", "label-365")
        item.status = PendingSortationCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingSortationCheckpointStatus.REQUESTED) }
    }
}
