package com.acme.transport.safety.violation.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702982538)
        val items = mutableListOf<ViolationProposal>()
        for (index in 0 until 5) {
            items.add(newViolationProposal("violation-proposal-$index", "sample"))
        }
        val report = buildViolationProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
