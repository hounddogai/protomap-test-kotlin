package com.acme.transport.terminal.crossdock.target

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703898343)
        val items = mutableListOf<GlobalCrossDockPlan>()
        for (index in 0 until 2) {
            items.add(newGlobalCrossDockPlan("global-cross-dock-plan-$index", "sample"))
        }
        val report = buildGlobalCrossDockPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
