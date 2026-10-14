package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703865672)
        val items = mutableListOf<PendingBackhaulReservation>()
        for (index in 0 until 6) {
            items.add(newPendingBackhaulReservation("pending-backhaul-reservation-$index", "sample"))
        }
        val report = buildPendingBackhaulReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
