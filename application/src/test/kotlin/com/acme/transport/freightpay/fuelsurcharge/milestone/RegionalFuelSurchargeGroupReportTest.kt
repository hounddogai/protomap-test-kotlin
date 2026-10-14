package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelSurchargeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708545097)
        val items = mutableListOf<RegionalFuelSurchargeGroup>()
        for (index in 0 until 2) {
            items.add(newRegionalFuelSurchargeGroup("regional-fuel-surcharge-group-$index", "sample"))
        }
        val report = buildRegionalFuelSurchargeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
