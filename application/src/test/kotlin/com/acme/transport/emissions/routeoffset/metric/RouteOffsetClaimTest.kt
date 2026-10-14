package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetClaimTest {
    @Test
    fun `starts in created`() {
        val item = newRouteOffsetClaim("code-260", "label-363")
        assertEquals(RouteOffsetClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRouteOffsetClaim("code-260", "label-363")
        try {
            item.moveTo(RouteOffsetClaimStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetClaimStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRouteOffsetClaim("code-260", "label-363")
        item.status = RouteOffsetClaimStatus.RETIRED
        assertFails("move") { item.moveTo(RouteOffsetClaimStatus.CREATED) }
    }
}
