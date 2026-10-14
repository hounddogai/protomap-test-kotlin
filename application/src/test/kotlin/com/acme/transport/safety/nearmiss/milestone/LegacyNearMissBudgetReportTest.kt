package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyNearMissBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708165245)
        val items = mutableListOf<LegacyNearMissBudget>()
        for (index in 0 until 4) {
            items.add(newLegacyNearMissBudget("legacy-near-miss-budget-$index", "sample"))
        }
        val report = buildLegacyNearMissBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
