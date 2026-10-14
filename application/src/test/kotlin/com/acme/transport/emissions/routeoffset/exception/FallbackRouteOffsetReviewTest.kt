package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackRouteOffsetReview("code-216", "name-934")
        assertEquals(FallbackRouteOffsetReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackRouteOffsetReview("code-216", "name-934")
        try {
            item.moveTo(FallbackRouteOffsetReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackRouteOffsetReview("code-216", "name-934")
        item.status = FallbackRouteOffsetReviewStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackRouteOffsetReviewStatus.IDLE) }
    }
}
