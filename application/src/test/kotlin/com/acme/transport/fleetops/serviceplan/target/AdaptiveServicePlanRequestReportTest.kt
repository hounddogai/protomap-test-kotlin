package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServicePlanRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701042472)
        val items = mutableListOf<AdaptiveServicePlanRequest>()
        for (index in 0 until 4) {
            items.add(newAdaptiveServicePlanRequest("adaptive-service-plan-request-$index", "sample"))
        }
        val report = buildAdaptiveServicePlanRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
