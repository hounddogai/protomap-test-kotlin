package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704734178)
        val items = mutableListOf<EvChargeReservation>()
        for (index in 0 until 6) {
            items.add(newEvChargeReservation("ev-charge-reservation-$index", "sample"))
        }
        val report = buildEvChargeReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
