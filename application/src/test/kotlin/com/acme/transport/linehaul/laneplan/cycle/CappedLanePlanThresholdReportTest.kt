package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701023763)
        val items = mutableListOf<CappedLanePlanThreshold>()
        for (index in 0 until 3) {
            items.add(newCappedLanePlanThreshold("capped-lane-plan-threshold-$index", "sample"))
        }
        val report = buildCappedLanePlanThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
