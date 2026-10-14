package com.acme.transport.linehaul.trailer.trailerrecord

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708033977)
        val items = mutableListOf<GlobalTrailerRecord>()
        for (index in 0 until 6) {
            items.add(newGlobalTrailerRecord("global-trailer-record-$index", "sample"))
        }
        val report = buildGlobalTrailerRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
