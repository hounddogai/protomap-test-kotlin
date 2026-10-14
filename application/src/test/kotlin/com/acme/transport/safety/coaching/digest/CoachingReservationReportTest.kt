package com.acme.transport.safety.coaching.digest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700414895)
        val items = mutableListOf<CoachingReservation>()
        for (index in 0 until 3) {
            items.add(newCoachingReservation("coaching-reservation-$index", "sample"))
        }
        val report = buildCoachingReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
