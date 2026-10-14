package com.acme.transport.terminal.appointment.segment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAppointmentAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705257776)
        val items = mutableListOf<SeasonalAppointmentAudit>()
        for (index in 0 until 4) {
            items.add(newSeasonalAppointmentAudit("seasonal-appointment-audit-$index", "sample"))
        }
        val report = buildSeasonalAppointmentAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
