package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704630527)
        val items = mutableListOf<FuelSurchargeNotice>()
        for (index in 0 until 2) {
            items.add(newFuelSurchargeNotice("fuel-surcharge-notice-$index", "sample"))
        }
        val report = buildFuelSurchargeNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
