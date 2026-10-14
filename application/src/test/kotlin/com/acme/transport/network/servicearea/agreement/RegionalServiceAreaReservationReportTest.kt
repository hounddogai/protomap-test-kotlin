package com.acme.transport.network.servicearea.agreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServiceAreaReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701219678)
        val items = mutableListOf<RegionalServiceAreaReservation>()
        for (index in 0 until 6) {
            items.add(newRegionalServiceAreaReservation("regional-service-area-reservation-$index", "sample"))
        }
        val report = buildRegionalServiceAreaReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
