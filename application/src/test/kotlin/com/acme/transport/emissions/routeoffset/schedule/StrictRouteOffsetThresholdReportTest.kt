package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRouteOffsetThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709240344)
        val items = mutableListOf<StrictRouteOffsetThreshold>()
        for (index in 0 until 2) {
            items.add(newStrictRouteOffsetThreshold("strict-route-offset-threshold-$index", "sample"))
        }
        val report = buildStrictRouteOffsetThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
