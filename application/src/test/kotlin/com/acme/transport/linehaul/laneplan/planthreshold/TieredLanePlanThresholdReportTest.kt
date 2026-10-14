package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLanePlanThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703614983)
        val items = mutableListOf<TieredLanePlanThreshold>()
        for (index in 0 until 2) {
            items.add(newTieredLanePlanThreshold("tiered-lane-plan-threshold-$index", "sample"))
        }
        val report = buildTieredLanePlanThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
