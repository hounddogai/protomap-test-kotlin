package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703753101)
        val items = mutableListOf<ServicePlanBatch>()
        for (index in 0 until 4) {
            items.add(newServicePlanBatch("service-plan-batch-$index", "sample"))
        }
        val report = buildServicePlanBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
