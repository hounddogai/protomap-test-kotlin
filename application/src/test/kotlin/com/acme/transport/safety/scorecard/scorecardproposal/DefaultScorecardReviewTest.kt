package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultScorecardReviewTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultScorecardReview("code-557", "title-296")
        assertEquals(DefaultScorecardReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDefaultScorecardReview("code-557", "title-296")
        try {
            item.moveTo(DefaultScorecardReviewStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultScorecardReviewStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultScorecardReview("code-557", "title-296")
        item.status = DefaultScorecardReviewStatus.FAILED
        assertFails("move") { item.moveTo(DefaultScorecardReviewStatus.NEW) }
    }
}
