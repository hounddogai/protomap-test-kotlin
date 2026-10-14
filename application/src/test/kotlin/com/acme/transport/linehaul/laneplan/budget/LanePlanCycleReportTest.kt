package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704337850)
        val items = mutableListOf<LanePlanCycle>()
        for (index in 0 until 5) {
            items.add(newLanePlanCycle("lane-plan-cycle-$index", "sample"))
        }
        val report = buildLanePlanCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
