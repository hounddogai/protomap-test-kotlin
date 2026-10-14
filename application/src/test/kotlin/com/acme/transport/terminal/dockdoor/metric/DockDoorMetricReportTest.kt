package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700953179)
        val items = mutableListOf<DockDoorMetric>()
        for (index in 0 until 3) {
            items.add(newDockDoorMetric("dock-door-metric-$index", "sample"))
        }
        val report = buildDockDoorMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
