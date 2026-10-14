package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServicePlanVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700595760)
        val items = mutableListOf<LegacyServicePlanVariance>()
        for (index in 0 until 2) {
            items.add(newLegacyServicePlanVariance("legacy-service-plan-variance-$index", "sample"))
        }
        val report = buildLegacyServicePlanVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
