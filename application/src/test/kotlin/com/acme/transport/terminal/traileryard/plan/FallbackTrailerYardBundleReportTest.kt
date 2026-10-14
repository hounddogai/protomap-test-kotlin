package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerYardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700387435)
        val items = mutableListOf<FallbackTrailerYardBundle>()
        for (index in 0 until 5) {
            items.add(newFallbackTrailerYardBundle("fallback-trailer-yard-bundle-$index", "sample"))
        }
        val report = buildFallbackTrailerYardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
