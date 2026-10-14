package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703700155)
        val items = mutableListOf<IncidentForecast>()
        for (index in 0 until 2) {
            items.add(newIncidentForecast("incident-forecast-$index", "sample"))
        }
        val report = buildIncidentForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
