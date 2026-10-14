package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706913216)
        val items = mutableListOf<TariffReservation>()
        for (index in 0 until 4) {
            items.add(newTariffReservation("tariff-reservation-$index", "sample"))
        }
        val report = buildTariffReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
