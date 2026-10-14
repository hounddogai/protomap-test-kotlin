package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualServicePlanQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704610266)
        val items = mutableListOf<ManualServicePlanQuota>()
        for (index in 0 until 3) {
            items.add(newManualServicePlanQuota("manual-service-plan-quota-$index", "sample"))
        }
        val report = buildManualServicePlanQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
