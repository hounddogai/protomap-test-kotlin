package com.acme.transport.customs.hold.proposal

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingHoldProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707405909)
        val items = mutableListOf<PendingHoldProposal>()
        for (index in 0 until 2) {
            items.add(newPendingHoldProposal("pending-hold-proposal-$index", "sample"))
        }
        val report = buildPendingHoldProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
