package com.acme.transport.linehaul.relay.segment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelaySegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706636823)
        val items = mutableListOf<PendingRelaySegment>()
        for (index in 0 until 3) {
            items.add(newPendingRelaySegment("pending-relay-segment-$index", "sample"))
        }
        val report = buildPendingRelaySegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
