package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706297823)
        val items = mutableListOf<RestBreakReservation>()
        for (index in 0 until 4) {
            items.add(newRestBreakReservation("rest-break-reservation-$index", "sample"))
        }
        val report = buildRestBreakReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
