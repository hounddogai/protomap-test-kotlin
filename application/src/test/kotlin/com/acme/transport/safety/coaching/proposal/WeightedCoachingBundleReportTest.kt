package com.acme.transport.safety.coaching.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCoachingBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702099644)
        val items = mutableListOf<WeightedCoachingBundle>()
        for (index in 0 until 2) {
            items.add(newWeightedCoachingBundle("weighted-coaching-bundle-$index", "sample"))
        }
        val report = buildWeightedCoachingBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
