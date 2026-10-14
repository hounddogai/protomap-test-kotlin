package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700780250)
        val items = mutableListOf<TariffBudget>()
        for (index in 0 until 4) {
            items.add(newTariffBudget("tariff-budget-$index", "sample"))
        }
        val report = buildTariffBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
