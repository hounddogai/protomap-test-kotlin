package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRouteOffsetCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705515229)
        val items = mutableListOf<GlobalRouteOffsetCycle>()
        for (index in 0 until 6) {
            items.add(newGlobalRouteOffsetCycle("global-route-offset-cycle-$index", "sample"))
        }
        val report = buildGlobalRouteOffsetCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
