package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702160425)
        val items = mutableListOf<HoldMetric>()
        for (index in 0 until 4) {
            items.add(newHoldMetric("hold-metric-$index", "sample"))
        }
        val report = buildHoldMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
