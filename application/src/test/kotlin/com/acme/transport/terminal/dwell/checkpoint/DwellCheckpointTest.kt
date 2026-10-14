package com.acme.transport.terminal.dwell.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellCheckpoint("code-515", "label-803")
        assertEquals(DwellCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newDwellCheckpoint("code-515", "label-803")
        try {
            item.moveTo(DwellCheckpointStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellCheckpointStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellCheckpoint("code-515", "label-803")
        item.status = DwellCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(DwellCheckpointStatus.DRAFT) }
    }
}
