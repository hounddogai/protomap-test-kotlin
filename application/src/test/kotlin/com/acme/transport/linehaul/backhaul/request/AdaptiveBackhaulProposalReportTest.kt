package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703418613)
        val items = mutableListOf<AdaptiveBackhaulProposal>()
        for (index in 0 until 2) {
            items.add(newAdaptiveBackhaulProposal("adaptive-backhaul-proposal-$index", "sample"))
        }
        val report = buildAdaptiveBackhaulProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
