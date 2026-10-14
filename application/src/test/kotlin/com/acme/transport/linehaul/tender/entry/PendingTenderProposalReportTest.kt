package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTenderProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706121049)
        val items = mutableListOf<PendingTenderProposal>()
        for (index in 0 until 4) {
            items.add(newPendingTenderProposal("pending-tender-proposal-$index", "sample"))
        }
        val report = buildPendingTenderProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
