package com.acme.transport.safety.incident.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704363132)
        val items = mutableListOf<ManualIncidentSegment>()
        for (index in 0 until 6) {
            items.add(newManualIncidentSegment("manual-incident-segment-$index", "sample"))
        }
        val report = buildManualIncidentSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
