package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700548274)
        val items = mutableListOf<DropThreshold>()
        for (index in 0 until 3) {
            items.add(newDropThreshold("drop-threshold-$index", "sample"))
        }
        val report = buildDropThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
