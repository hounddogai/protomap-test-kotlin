package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTransitTimeScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictTransitTimeScore("code-392", "name-252")
        assertEquals(StrictTransitTimeScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictTransitTimeScore("code-392", "name-252")
        try {
            item.moveTo(StrictTransitTimeScoreStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTransitTimeScoreStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictTransitTimeScore("code-392", "name-252")
        item.status = StrictTransitTimeScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictTransitTimeScoreStatus.PLANNED) }
    }
}
