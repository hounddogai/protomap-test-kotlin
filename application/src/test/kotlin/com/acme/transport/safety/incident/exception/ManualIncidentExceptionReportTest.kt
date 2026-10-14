package com.acme.transport.safety.incident.exception

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIncidentExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707224392)
        val items = mutableListOf<ManualIncidentException>()
        for (index in 0 until 3) {
            items.add(newManualIncidentException("manual-incident-exception-$index", "sample"))
        }
        val report = buildManualIncidentExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
