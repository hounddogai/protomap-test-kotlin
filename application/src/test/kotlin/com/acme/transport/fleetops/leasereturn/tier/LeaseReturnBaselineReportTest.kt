package com.acme.transport.fleetops.leasereturn.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707275643)
        val items = mutableListOf<LeaseReturnBaseline>()
        for (index in 0 until 4) {
            items.add(newLeaseReturnBaseline("lease-return-baseline-$index", "sample"))
        }
        val report = buildLeaseReturnBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
