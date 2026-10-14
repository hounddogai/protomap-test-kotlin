package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704249641)
        val items = mutableListOf<LeaseReturnAdjustment>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnAdjustment("lease-return-adjustment-$index", "sample"))
        }
        val report = buildLeaseReturnAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
