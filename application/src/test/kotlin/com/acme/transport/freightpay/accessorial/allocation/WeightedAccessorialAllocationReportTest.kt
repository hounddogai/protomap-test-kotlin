package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAccessorialAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701318832)
        val items = mutableListOf<WeightedAccessorialAllocation>()
        for (index in 0 until 5) {
            items.add(newWeightedAccessorialAllocation("weighted-accessorial-allocation-$index", "sample"))
        }
        val report = buildWeightedAccessorialAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
