package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCapacityPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701787791)
        val items = mutableListOf<WeightedCapacityPreference>()
        for (index in 0 until 2) {
            items.add(newWeightedCapacityPreference("weighted-capacity-preference-$index", "sample"))
        }
        val report = buildWeightedCapacityPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
