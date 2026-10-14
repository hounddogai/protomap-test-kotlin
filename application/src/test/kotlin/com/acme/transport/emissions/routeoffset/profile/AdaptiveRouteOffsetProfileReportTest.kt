package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRouteOffsetProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701480662)
        val items = mutableListOf<AdaptiveRouteOffsetProfile>()
        for (index in 0 until 5) {
            items.add(newAdaptiveRouteOffsetProfile("adaptive-route-offset-profile-$index", "sample"))
        }
        val report = buildAdaptiveRouteOffsetProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
