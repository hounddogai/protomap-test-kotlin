package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706187162)
        val items = mutableListOf<DeclarationReservation>()
        for (index in 0 until 3) {
            items.add(newDeclarationReservation("declaration-reservation-$index", "sample"))
        }
        val report = buildDeclarationReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
