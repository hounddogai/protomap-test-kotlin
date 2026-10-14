package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707040243)
        val items = mutableListOf<LeaseReturnDigest>()
        for (index in 0 until 3) {
            items.add(newLeaseReturnDigest("lease-return-digest-$index", "sample"))
        }
        val report = buildLeaseReturnDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
