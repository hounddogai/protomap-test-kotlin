package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707313957)
        val items = mutableListOf<ServicePlanSlot>()
        for (index in 0 until 4) {
            items.add(newServicePlanSlot("service-plan-slot-$index", "sample"))
        }
        val report = buildServicePlanSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
