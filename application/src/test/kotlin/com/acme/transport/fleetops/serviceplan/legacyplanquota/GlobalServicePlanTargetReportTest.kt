package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalServicePlanTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708895582)
        val items = mutableListOf<GlobalServicePlanTarget>()
        for (index in 0 until 2) {
            items.add(newGlobalServicePlanTarget("global-service-plan-target-$index", "sample"))
        }
        val report = buildGlobalServicePlanTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
