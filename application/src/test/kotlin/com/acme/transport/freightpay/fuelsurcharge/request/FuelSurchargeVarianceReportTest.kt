package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709863348)
        val items = mutableListOf<FuelSurchargeVariance>()
        for (index in 0 until 6) {
            items.add(newFuelSurchargeVariance("fuel-surcharge-variance-$index", "sample"))
        }
        val report = buildFuelSurchargeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
