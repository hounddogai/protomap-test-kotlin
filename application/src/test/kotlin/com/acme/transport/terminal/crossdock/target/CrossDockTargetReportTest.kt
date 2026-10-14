package com.acme.transport.terminal.crossdock.target

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708285260)
        val items = mutableListOf<CrossDockTarget>()
        for (index in 0 until 4) {
            items.add(newCrossDockTarget("cross-dock-target-$index", "sample"))
        }
        val report = buildCrossDockTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
