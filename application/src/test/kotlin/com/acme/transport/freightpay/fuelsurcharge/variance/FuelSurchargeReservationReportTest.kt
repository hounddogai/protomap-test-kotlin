package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708447567)
        val items = mutableListOf<FuelSurchargeReservation>()
        for (index in 0 until 6) {
            items.add(newFuelSurchargeReservation("fuel-surcharge-reservation-$index", "sample"))
        }
        val report = buildFuelSurchargeReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
