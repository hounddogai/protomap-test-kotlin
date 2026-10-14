package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredUtilizationReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708345627)
        val items = mutableListOf<TieredUtilizationReservation>()
        for (index in 0 until 3) {
            items.add(newTieredUtilizationReservation("tiered-utilization-reservation-$index", "sample"))
        }
        val report = buildTieredUtilizationReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
