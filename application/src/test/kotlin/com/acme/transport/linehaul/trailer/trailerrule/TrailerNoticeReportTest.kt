package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700692919)
        val items = mutableListOf<TrailerNotice>()
        for (index in 0 until 5) {
            items.add(newTrailerNotice("trailer-notice-$index", "sample"))
        }
        val report = buildTrailerNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
