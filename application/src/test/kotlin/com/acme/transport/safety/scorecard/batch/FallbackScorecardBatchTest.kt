package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackScorecardBatchTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackScorecardBatch("code-356", "label-53")
        assertEquals(FallbackScorecardBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackScorecardBatch("code-356", "label-53")
        try {
            item.moveTo(FallbackScorecardBatchStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackScorecardBatchStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackScorecardBatch("code-356", "label-53")
        item.status = FallbackScorecardBatchStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackScorecardBatchStatus.CREATED) }
    }
}
