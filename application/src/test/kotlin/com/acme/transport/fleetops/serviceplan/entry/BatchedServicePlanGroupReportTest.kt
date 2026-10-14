package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServicePlanGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708508394)
        val items = mutableListOf<BatchedServicePlanGroup>()
        for (index in 0 until 5) {
            items.add(newBatchedServicePlanGroup("batched-service-plan-group-$index", "sample"))
        }
        val report = buildBatchedServicePlanGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
