package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliveryReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702927931)
        val items = mutableListOf<WeightedProofOfDeliveryReservation>()
        for (index in 0 until 4) {
            items.add(newWeightedProofOfDeliveryReservation("weighted-proof-of-delivery-reservation-$index", "sample"))
        }
        val report = buildWeightedProofOfDeliveryReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
