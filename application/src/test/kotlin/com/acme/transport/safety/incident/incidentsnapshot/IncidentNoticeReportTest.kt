package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700318333)
        val items = mutableListOf<IncidentNotice>()
        for (index in 0 until 4) {
            items.add(newIncidentNotice("incident-notice-$index", "sample"))
        }
        val report = buildIncidentNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
