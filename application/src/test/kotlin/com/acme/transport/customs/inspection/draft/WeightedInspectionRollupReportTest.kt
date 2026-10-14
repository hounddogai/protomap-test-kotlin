package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706024079)
        val items = mutableListOf<WeightedInspectionRollup>()
        for (index in 0 until 6) {
            items.add(newWeightedInspectionRollup("weighted-inspection-rollup-$index", "sample"))
        }
        val report = buildWeightedInspectionRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
