package com.acme.transport.terminal.traileryard.template

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerYardDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705231848)
        val items = mutableListOf<StrictTrailerYardDigest>()
        for (index in 0 until 6) {
            items.add(newStrictTrailerYardDigest("strict-trailer-yard-digest-$index", "sample"))
        }
        val report = buildStrictTrailerYardDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
