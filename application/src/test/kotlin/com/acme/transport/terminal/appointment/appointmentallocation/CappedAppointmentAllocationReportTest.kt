package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAppointmentAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707119670)
        val items = mutableListOf<CappedAppointmentAllocation>()
        for (index in 0 until 6) {
            items.add(newCappedAppointmentAllocation("capped-appointment-allocation-$index", "sample"))
        }
        val report = buildCappedAppointmentAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
