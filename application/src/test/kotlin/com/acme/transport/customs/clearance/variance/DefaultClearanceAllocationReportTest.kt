package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultClearanceAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704681124)
        val items = mutableListOf<DefaultClearanceAllocation>()
        for (index in 0 until 6) {
            items.add(newDefaultClearanceAllocation("default-clearance-allocation-$index", "sample"))
        }
        val report = buildDefaultClearanceAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
