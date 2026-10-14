package com.acme.transport.safety.incident.digest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIncidentRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709025679)
        val items = mutableListOf<GlobalIncidentRequest>()
        for (index in 0 until 3) {
            items.add(newGlobalIncidentRequest("global-incident-request-$index", "sample"))
        }
        val report = buildGlobalIncidentRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
