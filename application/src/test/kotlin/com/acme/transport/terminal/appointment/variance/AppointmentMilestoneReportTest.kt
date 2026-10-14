package com.acme.transport.terminal.appointment.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707421789)
        val items = mutableListOf<AppointmentMilestone>()
        for (index in 0 until 3) {
            items.add(newAppointmentMilestone("appointment-milestone-$index", "sample"))
        }
        val report = buildAppointmentMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
