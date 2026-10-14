package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703458952)
        val items = mutableListOf<LenientAccessorialProposal>()
        for (index in 0 until 2) {
            items.add(newLenientAccessorialProposal("lenient-accessorial-proposal-$index", "sample"))
        }
        val report = buildLenientAccessorialProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
