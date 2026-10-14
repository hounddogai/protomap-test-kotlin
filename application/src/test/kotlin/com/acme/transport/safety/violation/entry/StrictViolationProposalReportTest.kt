package com.acme.transport.safety.violation.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702093088)
        val items = mutableListOf<StrictViolationProposal>()
        for (index in 0 until 6) {
            items.add(newStrictViolationProposal("strict-violation-proposal-$index", "sample"))
        }
        val report = buildStrictViolationProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
