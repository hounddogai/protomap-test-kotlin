package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newRouteOffsetReview("code-201", "name-363")
        assertEquals(RouteOffsetReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRouteOffsetReview("code-201", "name-363")
        try {
            item.moveTo(RouteOffsetReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRouteOffsetReview("code-201", "name-363")
        item.status = RouteOffsetReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RouteOffsetReviewStatus.PROPOSED) }
    }
}
