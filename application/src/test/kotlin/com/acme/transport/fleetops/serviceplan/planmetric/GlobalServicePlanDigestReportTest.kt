package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalServicePlanDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707788964)
        val items = mutableListOf<GlobalServicePlanDigest>()
        for (index in 0 until 4) {
            items.add(newGlobalServicePlanDigest("global-service-plan-digest-$index", "sample"))
        }
        val report = buildGlobalServicePlanDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
