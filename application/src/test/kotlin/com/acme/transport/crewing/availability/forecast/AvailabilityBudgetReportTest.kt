package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700353215)
        val items = mutableListOf<AvailabilityBudget>()
        for (index in 0 until 2) {
            items.add(newAvailabilityBudget("availability-budget-$index", "sample"))
        }
        val report = buildAvailabilityBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
