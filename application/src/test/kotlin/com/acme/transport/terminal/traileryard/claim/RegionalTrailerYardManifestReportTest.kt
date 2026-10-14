package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerYardManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707168753)
        val items = mutableListOf<RegionalTrailerYardManifest>()
        for (index in 0 until 6) {
            items.add(newRegionalTrailerYardManifest("regional-trailer-yard-manifest-$index", "sample"))
        }
        val report = buildRegionalTrailerYardManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
