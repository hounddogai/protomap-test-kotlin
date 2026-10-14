package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedScorecardBundleTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedScorecardBundle("code-67", "title-829")
        assertEquals(BatchedScorecardBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedScorecardBundle("code-67", "title-829")
        try {
            item.moveTo(BatchedScorecardBundleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedScorecardBundleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedScorecardBundle("code-67", "title-829")
        item.status = BatchedScorecardBundleStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedScorecardBundleStatus.CREATED) }
    }
}
