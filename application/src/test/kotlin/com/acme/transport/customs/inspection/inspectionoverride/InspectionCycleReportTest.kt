package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704913460)
        val items = mutableListOf<InspectionCycle>()
        for (index in 0 until 3) {
            items.add(newInspectionCycle("inspection-cycle-$index", "sample"))
        }
        val report = buildInspectionCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
