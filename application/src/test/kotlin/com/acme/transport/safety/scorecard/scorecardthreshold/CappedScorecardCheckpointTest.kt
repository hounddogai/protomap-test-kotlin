package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedScorecardCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedScorecardCheckpoint("code-134", "label-739")
        assertEquals(CappedScorecardCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newCappedScorecardCheckpoint("code-134", "label-739")
        try {
            item.moveTo(CappedScorecardCheckpointStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedScorecardCheckpointStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedScorecardCheckpoint("code-134", "label-739")
        item.status = CappedScorecardCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedScorecardCheckpointStatus.PLANNED) }
    }
}
