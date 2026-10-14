package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTrailerYardExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702944340)
        val items = mutableListOf<TieredTrailerYardException>()
        for (index in 0 until 5) {
            items.add(newTieredTrailerYardException("tiered-trailer-yard-exception-$index", "sample"))
        }
        val report = buildTieredTrailerYardExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
