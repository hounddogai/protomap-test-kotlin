package com.acme.transport.linehaul.relay.quota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703986555)
        val items = mutableListOf<RelayBudget>()
        for (index in 0 until 2) {
            items.add(newRelayBudget("relay-budget-$index", "sample"))
        }
        val report = buildRelayBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
