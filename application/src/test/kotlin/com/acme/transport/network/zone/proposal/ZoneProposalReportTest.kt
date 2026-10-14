package com.acme.transport.network.zone.proposal

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702963821)
        val items = mutableListOf<ZoneProposal>()
        for (index in 0 until 3) {
            items.add(newZoneProposal("zone-proposal-$index", "sample"))
        }
        val report = buildZoneProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
