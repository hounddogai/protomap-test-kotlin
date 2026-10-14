package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        assertEquals(TrailerYardCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        try {
            item.moveTo(TrailerYardCheckpointStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardCheckpointStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        item.status = TrailerYardCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerYardCheckpointStatus.PLANNED) }
    }
}
