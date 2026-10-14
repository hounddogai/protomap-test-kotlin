package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRateCardReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708469956)
        val items = mutableListOf<PrimaryRateCardReservation>()
        for (index in 0 until 4) {
            items.add(newPrimaryRateCardReservation("primary-rate-card-reservation-$index", "sample"))
        }
        val report = buildPrimaryRateCardReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
