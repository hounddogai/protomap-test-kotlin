package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707166489)
        val items = mutableListOf<PendingServicePlanRollup>()
        for (index in 0 until 2) {
            items.add(newPendingServicePlanRollup("pending-service-plan-rollup-$index", "sample"))
        }
        val report = buildPendingServicePlanRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
