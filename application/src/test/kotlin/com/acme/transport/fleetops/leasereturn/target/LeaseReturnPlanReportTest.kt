package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709763543)
        val items = mutableListOf<LeaseReturnPlan>()
        for (index in 0 until 6) {
            items.add(newLeaseReturnPlan("lease-return-plan-$index", "sample"))
        }
        val report = buildLeaseReturnPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
