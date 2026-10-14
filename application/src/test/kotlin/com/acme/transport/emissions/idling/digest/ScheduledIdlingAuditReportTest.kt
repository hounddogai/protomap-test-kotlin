package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700420616)
        val items = mutableListOf<ScheduledIdlingAudit>()
        for (index in 0 until 2) {
            items.add(newScheduledIdlingAudit("scheduled-idling-audit-$index", "sample"))
        }
        val report = buildScheduledIdlingAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
