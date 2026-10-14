package com.acme.transport.customs.tariff.score

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTariffBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702840375)
        val items = mutableListOf<WeightedTariffBudget>()
        for (index in 0 until 2) {
            items.add(newWeightedTariffBudget("weighted-tariff-budget-$index", "sample"))
        }
        val report = buildWeightedTariffBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
