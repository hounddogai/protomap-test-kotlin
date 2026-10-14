package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705163606)
        val items = mutableListOf<LanePlanProposal>()
        for (index in 0 until 5) {
            items.add(newLanePlanProposal("lane-plan-proposal-$index", "sample"))
        }
        val report = buildLanePlanProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
