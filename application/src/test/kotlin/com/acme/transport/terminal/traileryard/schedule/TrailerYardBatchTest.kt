package com.acme.transport.terminal.traileryard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerYardBatch("code-564", "name-548")
        assertEquals(TrailerYardBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTrailerYardBatch("code-564", "name-548")
        try {
            item.moveTo(TrailerYardBatchStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardBatchStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerYardBatch("code-564", "name-548")
        item.status = TrailerYardBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerYardBatchStatus.PROPOSED) }
    }
}
