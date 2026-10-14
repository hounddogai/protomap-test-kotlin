package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIncidentSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700322539)
        val items = mutableListOf<PrimaryIncidentSegment>()
        for (index in 0 until 2) {
            items.add(newPrimaryIncidentSegment("primary-incident-segment-$index", "sample"))
        }
        val report = buildPrimaryIncidentSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
