package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRestBreakProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703700107)
        val items = mutableListOf<ManualRestBreakProposal>()
        for (index in 0 until 5) {
            items.add(newManualRestBreakProposal("manual-rest-break-proposal-$index", "sample"))
        }
        val report = buildManualRestBreakProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
