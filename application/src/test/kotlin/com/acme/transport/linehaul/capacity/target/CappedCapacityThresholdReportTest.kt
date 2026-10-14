package com.acme.transport.linehaul.capacity.target

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCapacityThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700399425)
        val items = mutableListOf<CappedCapacityThreshold>()
        for (index in 0 until 6) {
            items.add(newCappedCapacityThreshold("capped-capacity-threshold-$index", "sample"))
        }
        val report = buildCappedCapacityThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
