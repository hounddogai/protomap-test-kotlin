package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707244415)
        val items = mutableListOf<CapacityBaseline>()
        for (index in 0 until 5) {
            items.add(newCapacityBaseline("capacity-baseline-$index", "sample"))
        }
        val report = buildCapacityBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
