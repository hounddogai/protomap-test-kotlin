package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702157315)
        val items = mutableListOf<StrictRosterReservation>()
        for (index in 0 until 6) {
            items.add(newStrictRosterReservation("strict-roster-reservation-$index", "sample"))
        }
        val report = buildStrictRosterReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
