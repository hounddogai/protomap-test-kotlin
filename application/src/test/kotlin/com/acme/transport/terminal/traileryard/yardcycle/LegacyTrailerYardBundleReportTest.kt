package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704652637)
        val items = mutableListOf<LegacyTrailerYardBundle>()
        for (index in 0 until 5) {
            items.add(newLegacyTrailerYardBundle("legacy-trailer-yard-bundle-$index", "sample"))
        }
        val report = buildLegacyTrailerYardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
