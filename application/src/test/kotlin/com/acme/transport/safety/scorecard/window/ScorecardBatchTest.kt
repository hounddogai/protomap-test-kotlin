package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newScorecardBatch("code-352", "title-767")
        assertEquals(ScorecardBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScorecardBatch("code-352", "title-767")
        try {
            item.moveTo(ScorecardBatchStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardBatchStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScorecardBatch("code-352", "title-767")
        item.status = ScorecardBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(ScorecardBatchStatus.PLANNED) }
    }
}
