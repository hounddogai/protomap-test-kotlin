package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerYardCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictTrailerYardCheckpoint("code-125", "title-954")
        assertEquals(StrictTrailerYardCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newStrictTrailerYardCheckpoint("code-125", "title-954")
        try {
            item.moveTo(StrictTrailerYardCheckpointStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerYardCheckpointStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictTrailerYardCheckpoint("code-125", "title-954")
        item.status = StrictTrailerYardCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictTrailerYardCheckpointStatus.PROPOSED) }
    }
}
