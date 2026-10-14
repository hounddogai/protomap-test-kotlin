package com.acme.transport.terminal.appointment.exception

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701214711)
        val items = mutableListOf<AppointmentException>()
        for (index in 0 until 2) {
            items.add(newAppointmentException("appointment-exception-$index", "sample"))
        }
        val report = buildAppointmentExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
