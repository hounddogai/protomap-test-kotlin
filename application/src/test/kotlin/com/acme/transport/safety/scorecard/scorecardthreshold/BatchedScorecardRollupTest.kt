package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedScorecardRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedScorecardRollup("code-296", "label-963")
        assertEquals(BatchedScorecardRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBatchedScorecardRollup("code-296", "label-963")
        try {
            item.moveTo(BatchedScorecardRollupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedScorecardRollupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedScorecardRollup("code-296", "label-963")
        item.status = BatchedScorecardRollupStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedScorecardRollupStatus.IDLE) }
    }
}
