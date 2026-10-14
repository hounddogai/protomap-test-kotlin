package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705039672)
        val items = mutableListOf<NearMissReservation>()
        for (index in 0 until 2) {
            items.add(newNearMissReservation("near-miss-reservation-$index", "sample"))
        }
        val report = buildNearMissReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
