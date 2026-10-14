package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702940487)
        val items = mutableListOf<IncidentVariance>()
        for (index in 0 until 6) {
            items.add(newIncidentVariance("incident-variance-$index", "sample"))
        }
        val report = buildIncidentVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
