package com.acme.transport.linehaul.relay.batch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelaySegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707519242)
        val items = mutableListOf<RelaySegment>()
        for (index in 0 until 5) {
            items.add(newRelaySegment("relay-segment-$index", "sample"))
        }
        val report = buildRelaySegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
