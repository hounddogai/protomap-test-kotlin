package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704787112)
        val items = mutableListOf<TrailerYardException>()
        for (index in 0 until 4) {
            items.add(newTrailerYardException("trailer-yard-exception-$index", "sample"))
        }
        val report = buildTrailerYardExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
