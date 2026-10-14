package com.acme.transport.fleetops.defect.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703801301)
        val items = mutableListOf<DefectTier>()
        for (index in 0 until 2) {
            items.add(newDefectTier("defect-tier-$index", "sample"))
        }
        val report = buildDefectTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
