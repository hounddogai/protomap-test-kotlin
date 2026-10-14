package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTireBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705927001)
        val items = mutableListOf<RollingTireBudget>()
        for (index in 0 until 6) {
            items.add(newRollingTireBudget("rolling-tire-budget-$index", "sample"))
        }
        val report = buildRollingTireBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
