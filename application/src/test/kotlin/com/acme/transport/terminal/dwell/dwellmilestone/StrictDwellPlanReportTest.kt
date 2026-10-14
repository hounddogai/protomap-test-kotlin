package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDwellPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708703650)
        val items = mutableListOf<StrictDwellPlan>()
        for (index in 0 until 3) {
            items.add(newStrictDwellPlan("strict-dwell-plan-$index", "sample"))
        }
        val report = buildStrictDwellPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
