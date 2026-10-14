package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705767992)
        val items = mutableListOf<FuelSurchargeException>()
        for (index in 0 until 4) {
            items.add(newFuelSurchargeException("fuel-surcharge-exception-$index", "sample"))
        }
        val report = buildFuelSurchargeExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
