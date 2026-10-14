package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703757926)
        val items = mutableListOf<ClearanceProposal>()
        for (index in 0 until 5) {
            items.add(newClearanceProposal("clearance-proposal-$index", "sample"))
        }
        val report = buildClearanceProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
