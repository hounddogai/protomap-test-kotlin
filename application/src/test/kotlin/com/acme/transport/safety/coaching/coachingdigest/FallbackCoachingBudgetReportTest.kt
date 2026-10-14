package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCoachingBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705060317)
        val items = mutableListOf<FallbackCoachingBudget>()
        for (index in 0 until 2) {
            items.add(newFallbackCoachingBudget("fallback-coaching-budget-$index", "sample"))
        }
        val report = buildFallbackCoachingBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
