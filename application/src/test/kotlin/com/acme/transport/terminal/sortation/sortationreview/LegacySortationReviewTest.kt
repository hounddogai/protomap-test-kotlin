package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySortationReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacySortationReview("code-552", "title-935")
        assertEquals(LegacySortationReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacySortationReview("code-552", "title-935")
        try {
            item.moveTo(LegacySortationReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySortationReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacySortationReview("code-552", "title-935")
        item.status = LegacySortationReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacySortationReviewStatus.PLANNED) }
    }
}
