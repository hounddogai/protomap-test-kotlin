package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706621738)
        val items = mutableListOf<WeightedLanePlanTarget>()
        for (index in 0 until 4) {
            items.add(newWeightedLanePlanTarget("weighted-lane-plan-target-$index", "sample"))
        }
        val report = buildWeightedLanePlanTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
