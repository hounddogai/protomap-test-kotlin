package com.acme.transport.safety.incident.incidentproposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIncidentProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706804754)
        val items = mutableListOf<CappedIncidentProposal>()
        for (index in 0 until 4) {
            items.add(newCappedIncidentProposal("capped-incident-proposal-$index", "sample"))
        }
        val report = buildCappedIncidentProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
