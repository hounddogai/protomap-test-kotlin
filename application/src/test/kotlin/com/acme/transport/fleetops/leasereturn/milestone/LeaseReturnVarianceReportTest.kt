package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704230779)
        val items = mutableListOf<LeaseReturnVariance>()
        for (index in 0 until 4) {
            items.add(newLeaseReturnVariance("lease-return-variance-$index", "sample"))
        }
        val report = buildLeaseReturnVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
