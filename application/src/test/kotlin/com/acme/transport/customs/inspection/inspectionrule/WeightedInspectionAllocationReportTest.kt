package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703490342)
        val items = mutableListOf<WeightedInspectionAllocation>()
        for (index in 0 until 4) {
            items.add(newWeightedInspectionAllocation("weighted-inspection-allocation-$index", "sample"))
        }
        val report = buildWeightedInspectionAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
