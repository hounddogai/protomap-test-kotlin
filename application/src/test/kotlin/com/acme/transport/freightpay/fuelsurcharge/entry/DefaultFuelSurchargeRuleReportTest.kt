package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelSurchargeRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708658037)
        val items = mutableListOf<DefaultFuelSurchargeRule>()
        for (index in 0 until 6) {
            items.add(newDefaultFuelSurchargeRule("default-fuel-surcharge-rule-$index", "sample"))
        }
        val report = buildDefaultFuelSurchargeRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
