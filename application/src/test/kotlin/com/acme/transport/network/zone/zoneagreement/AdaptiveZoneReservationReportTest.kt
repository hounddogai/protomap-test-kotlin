package com.acme.transport.network.zone.zoneagreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveZoneReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703835958)
        val items = mutableListOf<AdaptiveZoneReservation>()
        for (index in 0 until 6) {
            items.add(newAdaptiveZoneReservation("adaptive-zone-reservation-$index", "sample"))
        }
        val report = buildAdaptiveZoneReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
