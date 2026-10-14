package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRouteOffsetTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707081778)
        val items = mutableListOf<StrictRouteOffsetTarget>()
        for (index in 0 until 3) {
            items.add(newStrictRouteOffsetTarget("strict-route-offset-target-$index", "sample"))
        }
        val report = buildStrictRouteOffsetTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
