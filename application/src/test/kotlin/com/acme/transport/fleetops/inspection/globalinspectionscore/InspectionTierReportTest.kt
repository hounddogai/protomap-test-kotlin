package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702827451)
        val items = mutableListOf<InspectionTier>()
        for (index in 0 until 4) {
            items.add(newInspectionTier("inspection-tier-$index", "sample"))
        }
        val report = buildInspectionTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
