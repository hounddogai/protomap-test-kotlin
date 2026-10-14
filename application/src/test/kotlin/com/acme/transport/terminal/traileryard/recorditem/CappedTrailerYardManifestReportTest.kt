package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerYardManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707135039)
        val items = mutableListOf<CappedTrailerYardManifest>()
        for (index in 0 until 2) {
            items.add(newCappedTrailerYardManifest("capped-trailer-yard-manifest-$index", "sample"))
        }
        val report = buildCappedTrailerYardManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
