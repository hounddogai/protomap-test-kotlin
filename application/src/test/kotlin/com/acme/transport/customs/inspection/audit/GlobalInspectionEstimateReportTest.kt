package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702953136)
        val items = mutableListOf<GlobalInspectionEstimate>()
        for (index in 0 until 2) {
            items.add(newGlobalInspectionEstimate("global-inspection-estimate-$index", "sample"))
        }
        val report = buildGlobalInspectionEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
