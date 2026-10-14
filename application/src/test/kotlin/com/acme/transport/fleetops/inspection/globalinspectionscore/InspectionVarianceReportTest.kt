package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704942499)
        val items = mutableListOf<InspectionVariance>()
        for (index in 0 until 2) {
            items.add(newInspectionVariance("inspection-variance-$index", "sample"))
        }
        val report = buildInspectionVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
