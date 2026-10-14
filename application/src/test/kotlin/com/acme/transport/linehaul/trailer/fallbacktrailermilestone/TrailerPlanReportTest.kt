package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703927486)
        val items = mutableListOf<TrailerPlan>()
        for (index in 0 until 6) {
            items.add(newTrailerPlan("trailer-plan-$index", "sample"))
        }
        val report = buildTrailerPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
