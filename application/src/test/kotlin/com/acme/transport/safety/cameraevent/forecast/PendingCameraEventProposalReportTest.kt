package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCameraEventProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709547587)
        val items = mutableListOf<PendingCameraEventProposal>()
        for (index in 0 until 2) {
            items.add(newPendingCameraEventProposal("pending-camera-event-proposal-$index", "sample"))
        }
        val report = buildPendingCameraEventProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
