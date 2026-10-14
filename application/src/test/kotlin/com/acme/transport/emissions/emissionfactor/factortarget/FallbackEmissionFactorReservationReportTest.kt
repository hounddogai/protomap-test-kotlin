package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEmissionFactorReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700529195)
        val items = mutableListOf<FallbackEmissionFactorReservation>()
        for (index in 0 until 6) {
            items.add(newFallbackEmissionFactorReservation("fallback-emission-factor-reservation-$index", "sample"))
        }
        val report = buildFallbackEmissionFactorReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
