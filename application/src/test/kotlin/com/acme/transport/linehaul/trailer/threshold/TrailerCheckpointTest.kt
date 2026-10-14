package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerCheckpoint("code-379", "label-392")
        assertEquals(TrailerCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTrailerCheckpoint("code-379", "label-392")
        try {
            item.moveTo(TrailerCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerCheckpoint("code-379", "label-392")
        item.status = TrailerCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerCheckpointStatus.REQUESTED) }
    }
}
