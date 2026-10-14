package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706521679)
        val items = mutableListOf<PendingServicePlanMapping>()
        for (index in 0 until 3) {
            items.add(newPendingServicePlanMapping("pending-service-plan-mapping-$index", "sample"))
        }
        val report = buildPendingServicePlanMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
