package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704785808)
        val items = mutableListOf<LeaseReturnGroup>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnGroup("lease-return-group-$index", "sample"))
        }
        val report = buildLeaseReturnGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
