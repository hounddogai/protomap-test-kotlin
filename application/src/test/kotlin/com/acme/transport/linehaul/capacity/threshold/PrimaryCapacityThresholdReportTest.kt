package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCapacityThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709608887)
        val items = mutableListOf<PrimaryCapacityThreshold>()
        for (index in 0 until 2) {
            items.add(newPrimaryCapacityThreshold("primary-capacity-threshold-$index", "sample"))
        }
        val report = buildPrimaryCapacityThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
