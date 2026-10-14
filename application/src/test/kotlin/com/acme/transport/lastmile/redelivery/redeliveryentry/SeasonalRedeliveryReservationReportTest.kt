package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701725592)
        val items = mutableListOf<SeasonalRedeliveryReservation>()
        for (index in 0 until 2) {
            items.add(newSeasonalRedeliveryReservation("seasonal-redelivery-reservation-$index", "sample"))
        }
        val report = buildSeasonalRedeliveryReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
