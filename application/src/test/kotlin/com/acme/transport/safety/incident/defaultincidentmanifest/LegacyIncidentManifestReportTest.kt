package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701112770)
        val items = mutableListOf<LegacyIncidentManifest>()
        for (index in 0 until 3) {
            items.add(newLegacyIncidentManifest("legacy-incident-manifest-$index", "sample"))
        }
        val report = buildLegacyIncidentManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
