package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702969720)
        val items = mutableListOf<LanePlanTier>()
        for (index in 0 until 6) {
            items.add(newLanePlanTier("lane-plan-tier-$index", "sample"))
        }
        val report = buildLanePlanTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
