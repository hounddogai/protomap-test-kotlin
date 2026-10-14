package com.acme.transport.customs.inspection.target

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709078564)
        val items = mutableListOf<AdaptiveInspectionEstimate>()
        for (index in 0 until 2) {
            items.add(newAdaptiveInspectionEstimate("adaptive-inspection-estimate-$index", "sample"))
        }
        val report = buildAdaptiveInspectionEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
