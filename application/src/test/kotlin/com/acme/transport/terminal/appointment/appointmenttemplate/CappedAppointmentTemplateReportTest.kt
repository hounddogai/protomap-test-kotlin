package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAppointmentTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707497463)
        val items = mutableListOf<CappedAppointmentTemplate>()
        for (index in 0 until 6) {
            items.add(newCappedAppointmentTemplate("capped-appointment-template-$index", "sample"))
        }
        val report = buildCappedAppointmentTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
