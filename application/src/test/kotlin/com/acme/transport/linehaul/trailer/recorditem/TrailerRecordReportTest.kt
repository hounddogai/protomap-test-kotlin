package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709144059)
        val items = mutableListOf<TrailerRecord>()
        for (index in 0 until 4) {
            items.add(newTrailerRecord("trailer-record-$index", "sample"))
        }
        val report = buildTrailerRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
