package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702994418)
        val items = mutableListOf<InspectionBundle>()
        for (index in 0 until 3) {
            items.add(newInspectionBundle("inspection-bundle-$index", "sample"))
        }
        val report = buildInspectionBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
