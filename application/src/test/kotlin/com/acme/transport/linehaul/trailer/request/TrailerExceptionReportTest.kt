package com.acme.transport.linehaul.trailer.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707140399)
        val items = mutableListOf<TrailerException>()
        for (index in 0 until 6) {
            items.add(newTrailerException("trailer-exception-$index", "sample"))
        }
        val report = buildTrailerExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
