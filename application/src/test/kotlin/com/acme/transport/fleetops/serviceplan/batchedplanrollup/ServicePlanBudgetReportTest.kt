package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703567726)
        val items = mutableListOf<ServicePlanBudget>()
        for (index in 0 until 3) {
            items.add(newServicePlanBudget("service-plan-budget-$index", "sample"))
        }
        val report = buildServicePlanBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
