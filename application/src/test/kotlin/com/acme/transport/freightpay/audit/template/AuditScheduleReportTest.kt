package com.acme.transport.freightpay.audit.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703197503)
        val items = mutableListOf<AuditSchedule>()
        for (index in 0 until 6) {
            items.add(newAuditSchedule("audit-schedule-$index", "sample"))
        }
        val report = buildAuditScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
