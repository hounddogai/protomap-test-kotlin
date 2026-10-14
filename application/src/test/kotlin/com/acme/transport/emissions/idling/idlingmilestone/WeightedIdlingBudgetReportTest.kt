package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedIdlingBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704870269)
        val items = mutableListOf<WeightedIdlingBudget>()
        for (index in 0 until 5) {
            items.add(newWeightedIdlingBudget("weighted-idling-budget-$index", "sample"))
        }
        val report = buildWeightedIdlingBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
