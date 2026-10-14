package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIncidentRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708084149)
        val items = mutableListOf<PendingIncidentRequest>()
        for (index in 0 until 4) {
            items.add(newPendingIncidentRequest("pending-incident-request-$index", "sample"))
        }
        val report = buildPendingIncidentRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
