package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRouteOffsetVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708294798)
        val items = mutableListOf<ManualRouteOffsetVariance>()
        for (index in 0 until 5) {
            items.add(newManualRouteOffsetVariance("manual-route-offset-variance-$index", "sample"))
        }
        val report = buildManualRouteOffsetVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
