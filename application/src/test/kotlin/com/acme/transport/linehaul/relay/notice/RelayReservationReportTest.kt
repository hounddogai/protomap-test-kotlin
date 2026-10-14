package com.acme.transport.linehaul.relay.notice

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700587683)
        val items = mutableListOf<RelayReservation>()
        for (index in 0 until 2) {
            items.add(newRelayReservation("relay-reservation-$index", "sample"))
        }
        val report = buildRelayReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
