package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706999074)
        val items = mutableListOf<LegacyIncidentPreference>()
        for (index in 0 until 2) {
            items.add(newLegacyIncidentPreference("legacy-incident-preference-$index", "sample"))
        }
        val report = buildLegacyIncidentPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
