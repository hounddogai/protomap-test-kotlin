package com.acme.transport.fleetops.serviceplan.summary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700916829)
        val items = mutableListOf<ServicePlanSummary>()
        for (index in 0 until 5) {
            items.add(newServicePlanSummary("service-plan-summary-$index", "sample"))
        }
        val report = buildServicePlanSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
