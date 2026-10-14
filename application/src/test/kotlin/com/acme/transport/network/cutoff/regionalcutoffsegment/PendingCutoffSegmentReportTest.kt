package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707202016)
        val items = mutableListOf<PendingCutoffSegment>()
        for (index in 0 until 2) {
            items.add(newPendingCutoffSegment("pending-cutoff-segment-$index", "sample"))
        }
        val report = buildPendingCutoffSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
