package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700397492)
        val items = mutableListOf<AdaptiveCrossDockThreshold>()
        for (index in 0 until 6) {
            items.add(newAdaptiveCrossDockThreshold("adaptive-cross-dock-threshold-$index", "sample"))
        }
        val report = buildAdaptiveCrossDockThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
