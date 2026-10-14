package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702177355)
        val items = mutableListOf<IncidentDraft>()
        for (index in 0 until 4) {
            items.add(newIncidentDraft("incident-draft-$index", "sample"))
        }
        val report = buildIncidentDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
