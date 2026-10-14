package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704291470)
        val items = mutableListOf<FallbackLanePlanException>()
        for (index in 0 until 6) {
            items.add(newFallbackLanePlanException("fallback-lane-plan-exception-$index", "sample"))
        }
        val report = buildFallbackLanePlanExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
