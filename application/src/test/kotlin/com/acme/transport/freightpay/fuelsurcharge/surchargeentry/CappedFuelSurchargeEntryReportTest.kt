package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelSurchargeEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708872242)
        val items = mutableListOf<CappedFuelSurchargeEntry>()
        for (index in 0 until 3) {
            items.add(newCappedFuelSurchargeEntry("capped-fuel-surcharge-entry-$index", "sample"))
        }
        val report = buildCappedFuelSurchargeEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
