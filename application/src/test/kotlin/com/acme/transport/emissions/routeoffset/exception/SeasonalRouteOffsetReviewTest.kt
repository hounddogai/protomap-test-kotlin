package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRouteOffsetReviewTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalRouteOffsetReview("code-113", "label-397")
        assertEquals(SeasonalRouteOffsetReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalRouteOffsetReview("code-113", "label-397")
        try {
            item.moveTo(SeasonalRouteOffsetReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRouteOffsetReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalRouteOffsetReview("code-113", "label-397")
        item.status = SeasonalRouteOffsetReviewStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalRouteOffsetReviewStatus.CREATED) }
    }
}
