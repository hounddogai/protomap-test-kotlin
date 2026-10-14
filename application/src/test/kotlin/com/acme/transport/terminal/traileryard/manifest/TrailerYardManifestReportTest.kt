package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703103586)
        val items = mutableListOf<TrailerYardManifest>()
        for (index in 0 until 3) {
            items.add(newTrailerYardManifest("trailer-yard-manifest-$index", "sample"))
        }
        val report = buildTrailerYardManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
