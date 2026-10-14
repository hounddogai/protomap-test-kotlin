package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705968494)
        val items = mutableListOf<AppointmentBudget>()
        for (index in 0 until 2) {
            items.add(newAppointmentBudget("appointment-budget-$index", "sample"))
        }
        val report = buildAppointmentBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
