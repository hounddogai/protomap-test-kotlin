package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703495107)
        val items = mutableListOf<LenientProofOfDeliveryReservation>()
        for (index in 0 until 5) {
            items.add(newLenientProofOfDeliveryReservation("lenient-proof-of-delivery-reservation-$index", "sample"))
        }
        val report = buildLenientProofOfDeliveryReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
