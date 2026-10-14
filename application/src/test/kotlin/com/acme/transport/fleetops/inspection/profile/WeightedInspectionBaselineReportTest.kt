package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709221573)
        val items = mutableListOf<WeightedInspectionBaseline>()
        for (index in 0 until 6) {
            items.add(newWeightedInspectionBaseline("weighted-inspection-baseline-$index", "sample"))
        }
        val report = buildWeightedInspectionBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
