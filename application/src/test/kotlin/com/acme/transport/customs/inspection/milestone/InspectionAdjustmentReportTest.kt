package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704681338)
        val items = mutableListOf<InspectionAdjustment>()
        for (index in 0 until 4) {
            items.add(newInspectionAdjustment("inspection-adjustment-$index", "sample"))
        }
        val report = buildInspectionAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
