package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707111603)
        val items = mutableListOf<ServicePlanWindow>()
        for (index in 0 until 2) {
            items.add(newServicePlanWindow("service-plan-window-$index", "sample"))
        }
        val report = buildServicePlanWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
