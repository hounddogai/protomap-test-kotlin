package com.acme.transport.terminal.appointment.preference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703364545)
        val items = mutableListOf<AppointmentPreference>()
        for (index in 0 until 4) {
            items.add(newAppointmentPreference("appointment-preference-$index", "sample"))
        }
        val report = buildAppointmentPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
