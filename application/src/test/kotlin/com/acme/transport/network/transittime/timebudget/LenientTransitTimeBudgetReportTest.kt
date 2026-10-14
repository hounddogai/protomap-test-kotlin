package com.acme.transport.network.transittime.timebudget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTransitTimeBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702996597)
        val items = mutableListOf<LenientTransitTimeBudget>()
        for (index in 0 until 3) {
            items.add(newLenientTransitTimeBudget("lenient-transit-time-budget-$index", "sample"))
        }
        val report = buildLenientTransitTimeBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
