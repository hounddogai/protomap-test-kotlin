package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newScorecardCheckpoint("code-221", "name-241")
        assertEquals(ScorecardCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newScorecardCheckpoint("code-221", "name-241")
        try {
            item.moveTo(ScorecardCheckpointStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardCheckpointStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScorecardCheckpoint("code-221", "name-241")
        item.status = ScorecardCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(ScorecardCheckpointStatus.REQUESTED) }
    }
}
