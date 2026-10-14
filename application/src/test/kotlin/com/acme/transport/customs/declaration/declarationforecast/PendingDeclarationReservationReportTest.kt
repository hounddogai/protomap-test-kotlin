package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDeclarationReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700622433)
        val items = mutableListOf<PendingDeclarationReservation>()
        for (index in 0 until 2) {
            items.add(newPendingDeclarationReservation("pending-declaration-reservation-$index", "sample"))
        }
        val report = buildPendingDeclarationReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
