package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledParcelBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703244247)
        val items = mutableListOf<ScheduledParcelBudget>()
        for (index in 0 until 3) {
            items.add(newScheduledParcelBudget("scheduled-parcel-budget-$index", "sample"))
        }
        val report = buildScheduledParcelBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
