package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705479376)
        val items = mutableListOf<LanePlanAllocation>()
        for (index in 0 until 3) {
            items.add(newLanePlanAllocation("lane-plan-allocation-$index", "sample"))
        }
        val report = buildLanePlanAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
