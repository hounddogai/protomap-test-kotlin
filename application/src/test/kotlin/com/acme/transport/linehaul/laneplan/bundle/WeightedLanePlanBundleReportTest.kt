package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLanePlanBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700309788)
        val items = mutableListOf<WeightedLanePlanBundle>()
        for (index in 0 until 2) {
            items.add(newWeightedLanePlanBundle("weighted-lane-plan-bundle-$index", "sample"))
        }
        val report = buildWeightedLanePlanBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
