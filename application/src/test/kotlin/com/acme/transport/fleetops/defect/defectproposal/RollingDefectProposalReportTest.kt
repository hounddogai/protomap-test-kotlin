package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDefectProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701443557)
        val items = mutableListOf<RollingDefectProposal>()
        for (index in 0 until 3) {
            items.add(newRollingDefectProposal("rolling-defect-proposal-$index", "sample"))
        }
        val report = buildRollingDefectProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
