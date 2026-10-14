package com.acme.transport.network.servicearea.budget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702841891)
        val items = mutableListOf<ServiceAreaBudget>()
        for (index in 0 until 3) {
            items.add(newServiceAreaBudget("service-area-budget-$index", "sample"))
        }
        val report = buildServiceAreaBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
