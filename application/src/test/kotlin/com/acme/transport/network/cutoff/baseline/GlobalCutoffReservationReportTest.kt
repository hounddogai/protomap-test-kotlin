package com.acme.transport.network.cutoff.baseline

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCutoffReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708427833)
        val items = mutableListOf<GlobalCutoffReservation>()
        for (index in 0 until 5) {
            items.add(newGlobalCutoffReservation("global-cutoff-reservation-$index", "sample"))
        }
        val report = buildGlobalCutoffReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
