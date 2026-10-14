package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRouteOffsetAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalRouteOffsetAdjustment("code-511", "name-168")
        assertEquals(SeasonalRouteOffsetAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalRouteOffsetAdjustment("code-511", "name-168")
        try {
            item.moveTo(SeasonalRouteOffsetAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRouteOffsetAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalRouteOffsetAdjustment("code-511", "name-168")
        item.status = SeasonalRouteOffsetAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalRouteOffsetAdjustmentStatus.IDLE) }
    }
}
