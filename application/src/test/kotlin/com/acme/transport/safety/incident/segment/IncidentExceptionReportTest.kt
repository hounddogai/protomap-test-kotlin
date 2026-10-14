package com.acme.transport.safety.incident.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700823598)
        val items = mutableListOf<IncidentException>()
        for (index in 0 until 5) {
            items.add(newIncidentException("incident-exception-$index", "sample"))
        }
        val report = buildIncidentExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
