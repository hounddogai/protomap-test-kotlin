package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTireBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700569648)
        val items = mutableListOf<SeasonalTireBudget>()
        for (index in 0 until 3) {
            items.add(newSeasonalTireBudget("seasonal-tire-budget-$index", "sample"))
        }
        val report = buildSeasonalTireBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
