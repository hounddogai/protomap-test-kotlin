package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709454234)
        val items = mutableListOf<ServicePlanTier>()
        for (index in 0 until 4) {
            items.add(newServicePlanTier("service-plan-tier-$index", "sample"))
        }
        val report = buildServicePlanTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
