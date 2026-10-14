package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708752736)
        val items = mutableListOf<StrictIncidentMetric>()
        for (index in 0 until 6) {
            items.add(newStrictIncidentMetric("strict-incident-metric-$index", "sample"))
        }
        val report = buildStrictIncidentMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
