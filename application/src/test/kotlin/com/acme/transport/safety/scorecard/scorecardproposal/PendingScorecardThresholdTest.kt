package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingScorecardThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingScorecardThreshold("code-303", "title-406")
        assertEquals(PendingScorecardThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingScorecardThreshold("code-303", "title-406")
        try {
            item.moveTo(PendingScorecardThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingScorecardThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingScorecardThreshold("code-303", "title-406")
        item.status = PendingScorecardThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingScorecardThresholdStatus.REQUESTED) }
    }
}
