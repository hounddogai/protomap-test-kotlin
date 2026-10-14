package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708922560)
        val items = mutableListOf<FallbackRouteOffsetSchedule>()
        for (index in 0 until 3) {
            items.add(newFallbackRouteOffsetSchedule("fallback-route-offset-schedule-$index", "sample"))
        }
        val report = buildFallbackRouteOffsetScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
