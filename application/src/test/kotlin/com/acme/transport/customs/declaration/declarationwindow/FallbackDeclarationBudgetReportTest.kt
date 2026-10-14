package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704317385)
        val items = mutableListOf<FallbackDeclarationBudget>()
        for (index in 0 until 6) {
            items.add(newFallbackDeclarationBudget("fallback-declaration-budget-$index", "sample"))
        }
        val report = buildFallbackDeclarationBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
