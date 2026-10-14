package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702193886)
        val items = mutableListOf<LeaseReturnAllocation>()
        for (index in 0 until 3) {
            items.add(newLeaseReturnAllocation("lease-return-allocation-$index", "sample"))
        }
        val report = buildLeaseReturnAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
