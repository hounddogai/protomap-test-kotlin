package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706125156)
        val items = mutableListOf<StrictIncidentDraft>()
        for (index in 0 until 4) {
            items.add(newStrictIncidentDraft("strict-incident-draft-$index", "sample"))
        }
        val report = buildStrictIncidentDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
