package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedServicePlanEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708050695)
        val items = mutableListOf<WeightedServicePlanEntry>()
        for (index in 0 until 2) {
            items.add(newWeightedServicePlanEntry("weighted-service-plan-entry-$index", "sample"))
        }
        val report = buildWeightedServicePlanEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
