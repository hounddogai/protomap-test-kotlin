package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIdlingSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703355579)
        val items = mutableListOf<GlobalIdlingSegment>()
        for (index in 0 until 5) {
            items.add(newGlobalIdlingSegment("global-idling-segment-$index", "sample"))
        }
        val report = buildGlobalIdlingSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
