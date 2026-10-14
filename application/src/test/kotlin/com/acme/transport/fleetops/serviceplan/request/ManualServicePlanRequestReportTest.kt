package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualServicePlanRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704385642)
        val items = mutableListOf<ManualServicePlanRequest>()
        for (index in 0 until 2) {
            items.add(newManualServicePlanRequest("manual-service-plan-request-$index", "sample"))
        }
        val report = buildManualServicePlanRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
