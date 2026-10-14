package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCapacityDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705660479)
        val items = mutableListOf<WeightedCapacityDraft>()
        for (index in 0 until 2) {
            items.add(newWeightedCapacityDraft("weighted-capacity-draft-$index", "sample"))
        }
        val report = buildWeightedCapacityDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
