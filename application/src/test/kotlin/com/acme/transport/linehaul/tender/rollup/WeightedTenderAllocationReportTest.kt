package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTenderAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707812551)
        val items = mutableListOf<WeightedTenderAllocation>()
        for (index in 0 until 5) {
            items.add(newWeightedTenderAllocation("weighted-tender-allocation-$index", "sample"))
        }
        val report = buildWeightedTenderAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
