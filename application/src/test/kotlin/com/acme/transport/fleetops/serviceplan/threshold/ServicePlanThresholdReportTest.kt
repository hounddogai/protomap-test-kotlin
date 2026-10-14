package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706322365)
        val items = mutableListOf<ServicePlanThreshold>()
        for (index in 0 until 4) {
            items.add(newServicePlanThreshold("service-plan-threshold-$index", "sample"))
        }
        val report = buildServicePlanThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
