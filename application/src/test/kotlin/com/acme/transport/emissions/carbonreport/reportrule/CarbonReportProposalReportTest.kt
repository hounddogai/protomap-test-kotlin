package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700582401)
        val items = mutableListOf<CarbonReportProposal>()
        for (index in 0 until 5) {
            items.add(newCarbonReportProposal("carbon-report-proposal-$index", "sample"))
        }
        val report = buildCarbonReportProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
