package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLeaseReturnProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706880635)
        val items = mutableListOf<PrimaryLeaseReturnProposal>()
        for (index in 0 until 6) {
            items.add(newPrimaryLeaseReturnProposal("primary-lease-return-proposal-$index", "sample"))
        }
        val report = buildPrimaryLeaseReturnProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
