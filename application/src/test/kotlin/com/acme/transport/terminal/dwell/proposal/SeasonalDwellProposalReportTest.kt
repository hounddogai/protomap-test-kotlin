package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDwellProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708347122)
        val items = mutableListOf<SeasonalDwellProposal>()
        for (index in 0 until 2) {
            items.add(newSeasonalDwellProposal("seasonal-dwell-proposal-$index", "sample"))
        }
        val report = buildSeasonalDwellProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
