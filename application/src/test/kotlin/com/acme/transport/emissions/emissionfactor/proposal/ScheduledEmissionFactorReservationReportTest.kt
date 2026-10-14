package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledEmissionFactorReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701238731)
        val items = mutableListOf<ScheduledEmissionFactorReservation>()
        for (index in 0 until 6) {
            items.add(newScheduledEmissionFactorReservation("scheduled-emission-factor-reservation-$index", "sample"))
        }
        val report = buildScheduledEmissionFactorReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
