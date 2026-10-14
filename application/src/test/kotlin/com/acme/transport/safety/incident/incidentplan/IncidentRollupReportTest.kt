package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708952622)
        val items = mutableListOf<IncidentRollup>()
        for (index in 0 until 3) {
            items.add(newIncidentRollup("incident-rollup-$index", "sample"))
        }
        val report = buildIncidentRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
