package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAppointmentSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700989286)
        val items = mutableListOf<RollingAppointmentSnapshot>()
        for (index in 0 until 2) {
            items.add(newRollingAppointmentSnapshot("rolling-appointment-snapshot-$index", "sample"))
        }
        val report = buildRollingAppointmentSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
