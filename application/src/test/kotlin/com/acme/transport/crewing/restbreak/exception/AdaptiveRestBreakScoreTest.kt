package com.acme.transport.crewing.restbreak.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRestBreakScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveRestBreakScore("code-748", "label-922")
        assertEquals(AdaptiveRestBreakScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveRestBreakScore("code-748", "label-922")
        try {
            item.moveTo(AdaptiveRestBreakScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRestBreakScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveRestBreakScore("code-748", "label-922")
        item.status = AdaptiveRestBreakScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveRestBreakScoreStatus.PLANNED) }
    }
}
