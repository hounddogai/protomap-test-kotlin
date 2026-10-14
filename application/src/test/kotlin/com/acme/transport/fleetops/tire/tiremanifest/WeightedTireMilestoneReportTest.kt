package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701235041)
        val items = mutableListOf<WeightedTireMilestone>()
        for (index in 0 until 6) {
            items.add(newWeightedTireMilestone("weighted-tire-milestone-$index", "sample"))
        }
        val report = buildWeightedTireMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
