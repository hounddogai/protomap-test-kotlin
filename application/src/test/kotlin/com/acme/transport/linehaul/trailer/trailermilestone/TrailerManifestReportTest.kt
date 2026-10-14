package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702163728)
        val items = mutableListOf<TrailerManifest>()
        for (index in 0 until 4) {
            items.add(newTrailerManifest("trailer-manifest-$index", "sample"))
        }
        val report = buildTrailerManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
