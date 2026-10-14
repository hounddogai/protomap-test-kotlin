package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelSurchargeBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700629809)
        val items = mutableListOf<GlobalFuelSurchargeBaseline>()
        for (index in 0 until 6) {
            items.add(newGlobalFuelSurchargeBaseline("global-fuel-surcharge-baseline-$index", "sample"))
        }
        val report = buildGlobalFuelSurchargeBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
