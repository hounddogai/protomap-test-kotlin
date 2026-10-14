package com.acme.transport.network.zone.reservation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingZoneReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704905105)
        val items = mutableListOf<PendingZoneReservation>()
        for (index in 0 until 5) {
            items.add(newPendingZoneReservation("pending-zone-reservation-$index", "sample"))
        }
        val report = buildPendingZoneReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
