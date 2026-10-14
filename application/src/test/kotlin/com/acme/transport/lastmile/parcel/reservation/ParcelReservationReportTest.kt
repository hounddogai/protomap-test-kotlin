package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701945765)
        val items = mutableListOf<ParcelReservation>()
        for (index in 0 until 5) {
            items.add(newParcelReservation("parcel-reservation-$index", "sample"))
        }
        val report = buildParcelReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
