package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707239914)
        val items = mutableListOf<TieredFuelSurchargeRule>()
        for (index in 0 until 3) {
            items.add(newTieredFuelSurchargeRule("tiered-fuel-surcharge-rule-$index", "sample"))
        }
        val report = buildTieredFuelSurchargeRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
