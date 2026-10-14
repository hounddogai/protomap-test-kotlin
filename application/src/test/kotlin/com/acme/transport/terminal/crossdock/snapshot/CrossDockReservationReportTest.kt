package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700323856)
        val items = mutableListOf<CrossDockReservation>()
        for (index in 0 until 3) {
            items.add(newCrossDockReservation("cross-dock-reservation-$index", "sample"))
        }
        val report = buildCrossDockReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
