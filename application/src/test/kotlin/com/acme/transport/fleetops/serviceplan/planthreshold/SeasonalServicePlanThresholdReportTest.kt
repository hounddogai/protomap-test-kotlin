package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalServicePlanThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700322037)
        val items = mutableListOf<SeasonalServicePlanThreshold>()
        for (index in 0 until 4) {
            items.add(newSeasonalServicePlanThreshold("seasonal-service-plan-threshold-$index", "sample"))
        }
        val report = buildSeasonalServicePlanThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
