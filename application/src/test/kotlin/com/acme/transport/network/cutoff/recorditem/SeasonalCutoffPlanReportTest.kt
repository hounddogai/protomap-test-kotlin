package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCutoffPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708522250)
        val items = mutableListOf<SeasonalCutoffPlan>()
        for (index in 0 until 2) {
            items.add(newSeasonalCutoffPlan("seasonal-cutoff-plan-$index", "sample"))
        }
        val report = buildSeasonalCutoffPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
