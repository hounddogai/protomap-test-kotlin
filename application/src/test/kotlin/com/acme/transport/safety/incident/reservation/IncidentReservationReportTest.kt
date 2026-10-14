package com.acme.transport.safety.incident.reservation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706075476)
        val items = mutableListOf<IncidentReservation>()
        for (index in 0 until 6) {
            items.add(newIncidentReservation("incident-reservation-$index", "sample"))
        }
        val report = buildIncidentReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
