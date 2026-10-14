package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newRouteOffsetEstimate("code-851", "name-723")
        assertEquals(RouteOffsetEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRouteOffsetEstimate("code-851", "name-723")
        try {
            item.moveTo(RouteOffsetEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRouteOffsetEstimate("code-851", "name-723")
        item.status = RouteOffsetEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(RouteOffsetEstimateStatus.CREATED) }
    }
}
