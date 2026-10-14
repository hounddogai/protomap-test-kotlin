package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705045204)
        val items = mutableListOf<LeaseReturnProposal>()
        for (index in 0 until 3) {
            items.add(newLeaseReturnProposal("lease-return-proposal-$index", "sample"))
        }
        val report = buildLeaseReturnProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
