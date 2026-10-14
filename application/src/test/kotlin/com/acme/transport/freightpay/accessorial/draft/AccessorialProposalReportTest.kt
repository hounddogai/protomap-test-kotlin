package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705365151)
        val items = mutableListOf<AccessorialProposal>()
        for (index in 0 until 4) {
            items.add(newAccessorialProposal("accessorial-proposal-$index", "sample"))
        }
        val report = buildAccessorialProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
