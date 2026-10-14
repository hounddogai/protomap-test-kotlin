package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveScorecardCheckpointTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        assertEquals(AdaptiveScorecardCheckpointStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        try {
            item.moveTo(AdaptiveScorecardCheckpointStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveScorecardCheckpointStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveScorecardCheckpoint("code-639", "title-667")
        item.status = AdaptiveScorecardCheckpointStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveScorecardCheckpointStatus.REQUESTED) }
    }
}
