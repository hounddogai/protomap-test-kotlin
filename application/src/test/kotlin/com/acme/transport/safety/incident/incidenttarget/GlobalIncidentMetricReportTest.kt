package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIncidentMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706849469)
        val items = mutableListOf<GlobalIncidentMetric>()
        for (index in 0 until 5) {
            items.add(newGlobalIncidentMetric("global-incident-metric-$index", "sample"))
        }
        val report = buildGlobalIncidentMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
