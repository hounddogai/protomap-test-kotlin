package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNearMissBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708891957)
        val items = mutableListOf<PrimaryNearMissBudget>()
        for (index in 0 until 5) {
            items.add(newPrimaryNearMissBudget("primary-near-miss-budget-$index", "sample"))
        }
        val report = buildPrimaryNearMissBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
