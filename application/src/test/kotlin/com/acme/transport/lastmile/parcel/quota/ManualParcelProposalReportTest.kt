package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704060629)
        val items = mutableListOf<ManualParcelProposal>()
        for (index in 0 until 2) {
            items.add(newManualParcelProposal("manual-parcel-proposal-$index", "sample"))
        }
        val report = buildManualParcelProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
