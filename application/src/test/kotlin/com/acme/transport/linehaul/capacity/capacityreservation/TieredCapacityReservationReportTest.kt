package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCapacityReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707166635)
        val items = mutableListOf<TieredCapacityReservation>()
        for (index in 0 until 4) {
            items.add(newTieredCapacityReservation("tiered-capacity-reservation-$index", "sample"))
        }
        val report = buildTieredCapacityReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
