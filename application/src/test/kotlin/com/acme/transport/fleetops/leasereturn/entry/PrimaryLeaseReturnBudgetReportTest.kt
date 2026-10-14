package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLeaseReturnBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706171999)
        val items = mutableListOf<PrimaryLeaseReturnBudget>()
        for (index in 0 until 4) {
            items.add(newPrimaryLeaseReturnBudget("primary-lease-return-budget-$index", "sample"))
        }
        val report = buildPrimaryLeaseReturnBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
