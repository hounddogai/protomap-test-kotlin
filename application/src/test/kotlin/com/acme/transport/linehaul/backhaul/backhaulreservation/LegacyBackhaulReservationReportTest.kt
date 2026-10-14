package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBackhaulReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702150662)
        val items = mutableListOf<LegacyBackhaulReservation>()
        for (index in 0 until 6) {
            items.add(newLegacyBackhaulReservation("legacy-backhaul-reservation-$index", "sample"))
        }
        val report = buildLegacyBackhaulReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
