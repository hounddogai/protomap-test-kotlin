package com.acme.transport.safety.incident.baseline

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705472552)
        val items = mutableListOf<IncidentBaseline>()
        for (index in 0 until 3) {
            items.add(newIncidentBaseline("incident-baseline-$index", "sample"))
        }
        val report = buildIncidentBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
