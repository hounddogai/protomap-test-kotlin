package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIdlingSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703882080)
        val items = mutableListOf<StrictIdlingSegment>()
        for (index in 0 until 5) {
            items.add(newStrictIdlingSegment("strict-idling-segment-$index", "sample"))
        }
        val report = buildStrictIdlingSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
