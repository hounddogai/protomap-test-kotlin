package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRouteOffsetThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalRouteOffsetThreshold("code-777", "title-129")
        assertEquals(SeasonalRouteOffsetThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalRouteOffsetThreshold("code-777", "title-129")
        try {
            item.moveTo(SeasonalRouteOffsetThresholdStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRouteOffsetThresholdStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalRouteOffsetThreshold("code-777", "title-129")
        item.status = SeasonalRouteOffsetThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalRouteOffsetThresholdStatus.IDLE) }
    }
}
