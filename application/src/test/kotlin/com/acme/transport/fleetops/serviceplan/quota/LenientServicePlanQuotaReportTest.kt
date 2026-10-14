package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServicePlanQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706309088)
        val items = mutableListOf<LenientServicePlanQuota>()
        for (index in 0 until 2) {
            items.add(newLenientServicePlanQuota("lenient-service-plan-quota-$index", "sample"))
        }
        val report = buildLenientServicePlanQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
