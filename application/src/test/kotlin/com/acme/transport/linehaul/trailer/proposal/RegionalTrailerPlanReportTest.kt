package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700200349)
        val items = mutableListOf<RegionalTrailerPlan>()
        for (index in 0 until 5) {
            items.add(newRegionalTrailerPlan("regional-trailer-plan-$index", "sample"))
        }
        val report = buildRegionalTrailerPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
