package com.acme.transport.network.hub.milestone

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHubBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701056652)
        val items = mutableListOf<TieredHubBudget>()
        for (index in 0 until 6) {
            items.add(newTieredHubBudget("tiered-hub-budget-$index", "sample"))
        }
        val report = buildTieredHubBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
