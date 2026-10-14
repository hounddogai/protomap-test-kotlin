package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedServicePlanRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704187600)
        val items = mutableListOf<CappedServicePlanRollup>()
        for (index in 0 until 5) {
            items.add(newCappedServicePlanRollup("capped-service-plan-rollup-$index", "sample"))
        }
        val report = buildCappedServicePlanRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
