package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708038123)
        val items = mutableListOf<DefaultIdlingSegment>()
        for (index in 0 until 2) {
            items.add(newDefaultIdlingSegment("default-idling-segment-$index", "sample"))
        }
        val report = buildDefaultIdlingSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
