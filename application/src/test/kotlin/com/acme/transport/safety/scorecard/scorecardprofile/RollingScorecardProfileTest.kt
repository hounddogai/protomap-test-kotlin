package com.acme.transport.safety.scorecard.scorecardprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingScorecardProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingScorecardProfile("code-868", "label-665")
        assertEquals(RollingScorecardProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingScorecardProfile("code-868", "label-665")
        try {
            item.moveTo(RollingScorecardProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingScorecardProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingScorecardProfile("code-868", "label-665")
        item.status = RollingScorecardProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingScorecardProfileStatus.PLANNED) }
    }
}
