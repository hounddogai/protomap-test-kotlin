package com.acme.transport.terminal.appointment.segment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAppointmentBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709455605)
        val items = mutableListOf<GlobalAppointmentBudget>()
        for (index in 0 until 4) {
            items.add(newGlobalAppointmentBudget("global-appointment-budget-$index", "sample"))
        }
        val report = buildGlobalAppointmentBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
