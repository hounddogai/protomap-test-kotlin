package com.acme.transport.network.transittime.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTransitTimeReviewTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalTransitTimeReview("code-656", "title-552")
        assertEquals(SeasonalTransitTimeReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newSeasonalTransitTimeReview("code-656", "title-552")
        try {
            item.moveTo(SeasonalTransitTimeReviewStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTransitTimeReviewStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalTransitTimeReview("code-656", "title-552")
        item.status = SeasonalTransitTimeReviewStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalTransitTimeReviewStatus.CREATED) }
    }
}
