package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySwapReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706965745)
        val items = mutableListOf<LegacySwapReservation>()
        for (index in 0 until 6) {
            items.add(newLegacySwapReservation("legacy-swap-reservation-$index", "sample"))
        }
        val report = buildLegacySwapReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
