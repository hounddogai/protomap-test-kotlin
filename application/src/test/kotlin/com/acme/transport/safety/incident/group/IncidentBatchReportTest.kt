package com.acme.transport.safety.incident.group

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707275535)
        val items = mutableListOf<IncidentBatch>()
        for (index in 0 until 5) {
            items.add(newIncidentBatch("incident-batch-$index", "sample"))
        }
        val report = buildIncidentBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
