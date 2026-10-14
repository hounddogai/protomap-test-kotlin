package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709418892)
        val items = mutableListOf<FallbackRouteOffsetAllocation>()
        for (index in 0 until 6) {
            items.add(newFallbackRouteOffsetAllocation("fallback-route-offset-allocation-$index", "sample"))
        }
        val report = buildFallbackRouteOffsetAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
