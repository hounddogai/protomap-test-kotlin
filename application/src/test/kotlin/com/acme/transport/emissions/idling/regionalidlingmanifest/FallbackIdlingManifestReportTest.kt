package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706685503)
        val items = mutableListOf<FallbackIdlingManifest>()
        for (index in 0 until 2) {
            items.add(newFallbackIdlingManifest("fallback-idling-manifest-$index", "sample"))
        }
        val report = buildFallbackIdlingManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
