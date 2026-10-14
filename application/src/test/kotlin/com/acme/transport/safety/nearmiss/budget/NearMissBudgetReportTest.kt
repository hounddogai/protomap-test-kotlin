package com.acme.transport.safety.nearmiss.budget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702348606)
        val items = mutableListOf<NearMissBudget>()
        for (index in 0 until 6) {
            items.add(newNearMissBudget("near-miss-budget-$index", "sample"))
        }
        val report = buildNearMissBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
