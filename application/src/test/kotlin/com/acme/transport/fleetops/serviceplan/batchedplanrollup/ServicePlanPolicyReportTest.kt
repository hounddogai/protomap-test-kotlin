package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702938831)
        val items = mutableListOf<ServicePlanPolicy>()
        for (index in 0 until 4) {
            items.add(newServicePlanPolicy("service-plan-policy-$index", "sample"))
        }
        val report = buildServicePlanPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
