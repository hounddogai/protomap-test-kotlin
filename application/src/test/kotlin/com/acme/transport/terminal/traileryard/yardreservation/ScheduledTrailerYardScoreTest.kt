package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerYardScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledTrailerYardScore("code-999", "name-759")
        assertEquals(ScheduledTrailerYardScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledTrailerYardScore("code-999", "name-759")
        try {
            item.moveTo(ScheduledTrailerYardScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerYardScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledTrailerYardScore("code-999", "name-759")
        item.status = ScheduledTrailerYardScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledTrailerYardScoreStatus.PROPOSED) }
    }
}
