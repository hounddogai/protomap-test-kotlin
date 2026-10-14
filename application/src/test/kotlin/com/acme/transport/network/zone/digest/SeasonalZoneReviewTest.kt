package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalZoneReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalZoneReview("code-20", "name-183")
        assertEquals(SeasonalZoneReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalZoneReview("code-20", "name-183")
        try {
            item.moveTo(SeasonalZoneReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalZoneReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalZoneReview("code-20", "name-183")
        item.status = SeasonalZoneReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalZoneReviewStatus.REQUESTED) }
    }
}
