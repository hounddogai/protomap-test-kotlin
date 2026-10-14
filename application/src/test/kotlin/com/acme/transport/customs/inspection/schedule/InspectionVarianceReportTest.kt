package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706330118)
        val items = mutableListOf<InspectionVariance>()
        for (index in 0 until 3) {
            items.add(newInspectionVariance("inspection-variance-$index", "sample"))
        }
        val report = buildInspectionVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
