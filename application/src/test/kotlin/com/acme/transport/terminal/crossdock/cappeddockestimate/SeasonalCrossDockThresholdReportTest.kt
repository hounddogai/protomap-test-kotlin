package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCrossDockThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703125950)
        val items = mutableListOf<SeasonalCrossDockThreshold>()
        for (index in 0 until 6) {
            items.add(newSeasonalCrossDockThreshold("seasonal-cross-dock-threshold-$index", "sample"))
        }
        val report = buildSeasonalCrossDockThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
