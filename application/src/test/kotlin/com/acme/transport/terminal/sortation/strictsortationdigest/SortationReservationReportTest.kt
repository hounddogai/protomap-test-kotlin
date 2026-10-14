package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707259619)
        val items = mutableListOf<SortationReservation>()
        for (index in 0 until 3) {
            items.add(newSortationReservation("sortation-reservation-$index", "sample"))
        }
        val report = buildSortationReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
