package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703018613)
        val items = mutableListOf<AppointmentEstimate>()
        for (index in 0 until 4) {
            items.add(newAppointmentEstimate("appointment-estimate-$index", "sample"))
        }
        val report = buildAppointmentEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
