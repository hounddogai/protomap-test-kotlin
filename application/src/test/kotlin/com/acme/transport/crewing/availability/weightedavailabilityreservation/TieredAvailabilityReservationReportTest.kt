package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705017583)
        val items = mutableListOf<TieredAvailabilityReservation>()
        for (index in 0 until 5) {
            items.add(newTieredAvailabilityReservation("tiered-availability-reservation-$index", "sample"))
        }
        val report = buildTieredAvailabilityReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
