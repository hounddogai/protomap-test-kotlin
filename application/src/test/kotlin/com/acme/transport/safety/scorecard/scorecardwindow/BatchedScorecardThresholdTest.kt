package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedScorecardThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        assertEquals(BatchedScorecardThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        try {
            item.moveTo(BatchedScorecardThresholdStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedScorecardThresholdStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        item.status = BatchedScorecardThresholdStatus.FAILED
        assertFails("move") { item.moveTo(BatchedScorecardThresholdStatus.NEW) }
    }
}
