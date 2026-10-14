package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRosterPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705723785)
        val items = mutableListOf<TieredRosterPlan>()
        for (index in 0 until 2) {
            items.add(newTieredRosterPlan("tiered-roster-plan-$index", "sample"))
        }
        val report = buildTieredRosterPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
