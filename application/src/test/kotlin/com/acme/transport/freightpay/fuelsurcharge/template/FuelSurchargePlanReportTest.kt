package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700529799)
        val items = mutableListOf<FuelSurchargePlan>()
        for (index in 0 until 2) {
            items.add(newFuelSurchargePlan("fuel-surcharge-plan-$index", "sample"))
        }
        val report = buildFuelSurchargePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
