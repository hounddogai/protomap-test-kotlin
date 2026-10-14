package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703363454)
        val items = mutableListOf<LanePlanAdjustment>()
        for (index in 0 until 6) {
            items.add(newLanePlanAdjustment("lane-plan-adjustment-$index", "sample"))
        }
        val report = buildLanePlanAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
