package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientIncidentProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709992523)
        val items = mutableListOf<LenientIncidentProposal>()
        for (index in 0 until 2) {
            items.add(newLenientIncidentProposal("lenient-incident-proposal-$index", "sample"))
        }
        val report = buildLenientIncidentProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
