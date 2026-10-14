package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelSurchargeWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701606722)
        val items = mutableListOf<DefaultFuelSurchargeWindow>()
        for (index in 0 until 2) {
            items.add(newDefaultFuelSurchargeWindow("default-fuel-surcharge-window-$index", "sample"))
        }
        val report = buildDefaultFuelSurchargeWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
