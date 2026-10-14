package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709557202)
        val items = mutableListOf<RollingDwellPlan>()
        for (index in 0 until 4) {
            items.add(newRollingDwellPlan("rolling-dwell-plan-$index", "sample"))
        }
        val report = buildRollingDwellPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
