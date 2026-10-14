package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701776322)
        val items = mutableListOf<DutyPeriodProposal>()
        for (index in 0 until 2) {
            items.add(newDutyPeriodProposal("duty-period-proposal-$index", "sample"))
        }
        val report = buildDutyPeriodProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
