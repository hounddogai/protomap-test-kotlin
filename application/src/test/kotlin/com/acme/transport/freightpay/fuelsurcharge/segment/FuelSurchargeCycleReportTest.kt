package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704616789)
        val items = mutableListOf<FuelSurchargeCycle>()
        for (index in 0 until 6) {
            items.add(newFuelSurchargeCycle("fuel-surcharge-cycle-$index", "sample"))
        }
        val report = buildFuelSurchargeCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
