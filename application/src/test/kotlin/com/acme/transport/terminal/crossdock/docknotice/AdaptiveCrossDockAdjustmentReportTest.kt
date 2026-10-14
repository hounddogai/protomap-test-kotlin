package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705628641)
        val items = mutableListOf<AdaptiveCrossDockAdjustment>()
        for (index in 0 until 6) {
            items.add(newAdaptiveCrossDockAdjustment("adaptive-cross-dock-adjustment-$index", "sample"))
        }
        val report = buildAdaptiveCrossDockAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
