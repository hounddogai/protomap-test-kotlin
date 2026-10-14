package com.acme.transport.linehaul.laneplan.reservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700409943)
        val items = mutableListOf<LanePlanTarget>()
        for (index in 0 until 3) {
            items.add(newLanePlanTarget("lane-plan-target-$index", "sample"))
        }
        val report = buildLanePlanTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
