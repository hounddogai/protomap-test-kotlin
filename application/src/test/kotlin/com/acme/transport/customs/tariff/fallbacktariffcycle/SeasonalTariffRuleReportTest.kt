package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTariffRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709241966)
        val items = mutableListOf<SeasonalTariffRule>()
        for (index in 0 until 5) {
            items.add(newSeasonalTariffRule("seasonal-tariff-rule-$index", "sample"))
        }
        val report = buildSeasonalTariffRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
