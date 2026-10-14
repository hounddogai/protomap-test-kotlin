package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newRouteOffsetScore("code-112", "label-103")
        assertEquals(RouteOffsetScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRouteOffsetScore("code-112", "label-103")
        try {
            item.moveTo(RouteOffsetScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRouteOffsetScore("code-112", "label-103")
        item.status = RouteOffsetScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RouteOffsetScoreStatus.PROPOSED) }
    }
}
