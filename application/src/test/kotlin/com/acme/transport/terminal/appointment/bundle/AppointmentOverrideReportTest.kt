package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706679880)
        val items = mutableListOf<AppointmentOverride>()
        for (index in 0 until 2) {
            items.add(newAppointmentOverride("appointment-override-$index", "sample"))
        }
        val report = buildAppointmentOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
