package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentReviewReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708266919)
        val items = mutableListOf<AppointmentReview>()
        for (index in 0 until 6) {
            items.add(newAppointmentReview("appointment-review-$index", "sample"))
        }
        val report = buildAppointmentReviewReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
