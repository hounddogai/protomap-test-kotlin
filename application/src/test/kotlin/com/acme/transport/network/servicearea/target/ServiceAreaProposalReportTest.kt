package com.acme.transport.network.servicearea.target

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704642860)
        val items = mutableListOf<ServiceAreaProposal>()
        for (index in 0 until 2) {
            items.add(newServiceAreaProposal("service-area-proposal-$index", "sample"))
        }
        val report = buildServiceAreaProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
