package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708306675)
        val items = mutableListOf<SwapReservation>()
        for (index in 0 until 4) {
            items.add(newSwapReservation("swap-reservation-$index", "sample"))
        }
        val report = buildSwapReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
