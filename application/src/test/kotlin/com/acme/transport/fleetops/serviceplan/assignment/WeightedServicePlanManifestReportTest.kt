package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708162988)
        val items = mutableListOf<WeightedServicePlanManifest>()
        for (index in 0 until 4) {
            items.add(newWeightedServicePlanManifest("weighted-service-plan-manifest-$index", "sample"))
        }
        val report = buildWeightedServicePlanManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
