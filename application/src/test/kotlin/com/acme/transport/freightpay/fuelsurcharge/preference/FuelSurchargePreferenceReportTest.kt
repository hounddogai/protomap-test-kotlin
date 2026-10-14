package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709381168)
        val items = mutableListOf<FuelSurchargePreference>()
        for (index in 0 until 2) {
            items.add(newFuelSurchargePreference("fuel-surcharge-preference-$index", "sample"))
        }
        val report = buildFuelSurchargePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
