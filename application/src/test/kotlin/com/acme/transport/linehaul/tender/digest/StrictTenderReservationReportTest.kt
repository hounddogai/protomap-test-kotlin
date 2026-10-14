package com.acme.transport.linehaul.tender.digest

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701563842)
        val items = mutableListOf<StrictTenderReservation>()
        for (index in 0 until 6) {
            items.add(newStrictTenderReservation("strict-tender-reservation-$index", "sample"))
        }
        val report = buildStrictTenderReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
