package com.acme.transport.safety.violation.violationpolicy

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702491303)
        val items = mutableListOf<GlobalViolationProposal>()
        for (index in 0 until 3) {
            items.add(newGlobalViolationProposal("global-violation-proposal-$index", "sample"))
        }
        val report = buildGlobalViolationProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
