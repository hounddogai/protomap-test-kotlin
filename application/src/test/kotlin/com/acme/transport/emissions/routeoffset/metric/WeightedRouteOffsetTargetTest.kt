package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRouteOffsetTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRouteOffsetTarget("code-236", "title-218")
        assertEquals(WeightedRouteOffsetTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedRouteOffsetTarget("code-236", "title-218")
        try {
            item.moveTo(WeightedRouteOffsetTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRouteOffsetTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRouteOffsetTarget("code-236", "title-218")
        item.status = WeightedRouteOffsetTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRouteOffsetTargetStatus.PLANNED) }
    }
}
