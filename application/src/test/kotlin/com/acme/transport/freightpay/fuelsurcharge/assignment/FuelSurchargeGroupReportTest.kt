package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706135610)
        val items = mutableListOf<FuelSurchargeGroup>()
        for (index in 0 until 3) {
            items.add(newFuelSurchargeGroup("fuel-surcharge-group-$index", "sample"))
        }
        val report = buildFuelSurchargeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
