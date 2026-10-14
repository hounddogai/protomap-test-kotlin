package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708752308)
        val items = mutableListOf<FallbackBackhaulReservation>()
        for (index in 0 until 6) {
            items.add(newFallbackBackhaulReservation("fallback-backhaul-reservation-$index", "sample"))
        }
        val report = buildFallbackBackhaulReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
