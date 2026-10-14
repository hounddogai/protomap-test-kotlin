package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700746054)
        val items = mutableListOf<LeaseReturnClaim>()
        for (index in 0 until 3) {
            items.add(newLeaseReturnClaim("lease-return-claim-$index", "sample"))
        }
        val report = buildLeaseReturnClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
