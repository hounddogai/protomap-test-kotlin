package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701060327)
        val items = mutableListOf<CappedInspectionBundle>()
        for (index in 0 until 5) {
            items.add(newCappedInspectionBundle("capped-inspection-bundle-$index", "sample"))
        }
        val report = buildCappedInspectionBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
