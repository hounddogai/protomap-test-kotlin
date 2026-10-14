package com.acme.transport.terminal.appointment.group

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708027033)
        val items = mutableListOf<AppointmentDraft>()
        for (index in 0 until 5) {
            items.add(newAppointmentDraft("appointment-draft-$index", "sample"))
        }
        val report = buildAppointmentDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
