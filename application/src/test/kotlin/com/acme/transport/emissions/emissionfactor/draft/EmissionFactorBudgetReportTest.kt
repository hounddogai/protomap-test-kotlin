package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702279822)
        val items = mutableListOf<EmissionFactorBudget>()
        for (index in 0 until 2) {
            items.add(newEmissionFactorBudget("emission-factor-budget-$index", "sample"))
        }
        val report = buildEmissionFactorBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
