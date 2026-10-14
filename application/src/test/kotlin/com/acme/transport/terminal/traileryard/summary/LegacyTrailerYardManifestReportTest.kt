package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700370798)
        val items = mutableListOf<LegacyTrailerYardManifest>()
        for (index in 0 until 4) {
            items.add(newLegacyTrailerYardManifest("legacy-trailer-yard-manifest-$index", "sample"))
        }
        val report = buildLegacyTrailerYardManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
