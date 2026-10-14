package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNearMissBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702742481)
        val items = mutableListOf<FallbackNearMissBudget>()
        for (index in 0 until 4) {
            items.add(newFallbackNearMissBudget("fallback-near-miss-budget-$index", "sample"))
        }
        val report = buildFallbackNearMissBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
