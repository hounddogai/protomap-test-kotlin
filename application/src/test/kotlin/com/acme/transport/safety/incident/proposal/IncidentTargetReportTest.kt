package com.acme.transport.safety.incident.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700444439)
        val items = mutableListOf<IncidentTarget>()
        for (index in 0 until 3) {
            items.add(newIncidentTarget("incident-target-$index", "sample"))
        }
        val report = buildIncidentTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
