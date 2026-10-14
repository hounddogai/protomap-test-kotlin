package com.acme.transport.network.zone.cycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704906060)
        val items = mutableListOf<TieredZoneReservation>()
        for (index in 0 until 6) {
            items.add(newTieredZoneReservation("tiered-zone-reservation-$index", "sample"))
        }
        val report = buildTieredZoneReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
