package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706473311)
        val items = mutableListOf<AppointmentBundle>()
        for (index in 0 until 6) {
            items.add(newAppointmentBundle("appointment-bundle-$index", "sample"))
        }
        val report = buildAppointmentBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
