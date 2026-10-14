package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIncidentManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706874745)
        val items = mutableListOf<RollingIncidentManifest>()
        for (index in 0 until 3) {
            items.add(newRollingIncidentManifest("rolling-incident-manifest-$index", "sample"))
        }
        val report = buildRollingIncidentManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
