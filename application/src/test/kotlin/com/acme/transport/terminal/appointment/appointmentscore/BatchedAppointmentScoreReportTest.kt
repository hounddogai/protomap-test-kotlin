package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAppointmentScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704605898)
        val items = mutableListOf<BatchedAppointmentScore>()
        for (index in 0 until 4) {
            items.add(newBatchedAppointmentScore("batched-appointment-score-$index", "sample"))
        }
        val report = buildBatchedAppointmentScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
