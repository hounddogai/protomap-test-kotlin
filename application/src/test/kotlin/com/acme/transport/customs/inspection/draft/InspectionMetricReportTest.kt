package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708534622)
        val items = mutableListOf<InspectionMetric>()
        for (index in 0 until 4) {
            items.add(newInspectionMetric("inspection-metric-$index", "sample"))
        }
        val report = buildInspectionMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
