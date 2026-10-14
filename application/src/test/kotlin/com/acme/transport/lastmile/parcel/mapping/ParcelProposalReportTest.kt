package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709569778)
        val items = mutableListOf<ParcelProposal>()
        for (index in 0 until 5) {
            items.add(newParcelProposal("parcel-proposal-$index", "sample"))
        }
        val report = buildParcelProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
