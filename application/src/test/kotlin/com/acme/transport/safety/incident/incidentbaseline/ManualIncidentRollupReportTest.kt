package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704268786)
        val items = mutableListOf<ManualIncidentRollup>()
        for (index in 0 until 6) {
            items.add(newManualIncidentRollup("manual-incident-rollup-$index", "sample"))
        }
        val report = buildManualIncidentRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
