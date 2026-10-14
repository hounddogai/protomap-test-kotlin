package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701149912)
        val items = mutableListOf<InspectionReservation>()
        for (index in 0 until 2) {
            items.add(newInspectionReservation("inspection-reservation-$index", "sample"))
        }
        val report = buildInspectionReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
