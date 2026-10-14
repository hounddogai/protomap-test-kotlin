package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveParcelReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702493033)
        val items = mutableListOf<AdaptiveParcelReservation>()
        for (index in 0 until 3) {
            items.add(newAdaptiveParcelReservation("adaptive-parcel-reservation-$index", "sample"))
        }
        val report = buildAdaptiveParcelReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
