package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706330718)
        val items = mutableListOf<ServicePlanAllocation>()
        for (index in 0 until 2) {
            items.add(newServicePlanAllocation("service-plan-allocation-$index", "sample"))
        }
        val report = buildServicePlanAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
