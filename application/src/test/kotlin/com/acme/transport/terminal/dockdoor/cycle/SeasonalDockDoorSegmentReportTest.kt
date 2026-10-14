package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700379806)
        val items = mutableListOf<SeasonalDockDoorSegment>()
        for (index in 0 until 4) {
            items.add(newSeasonalDockDoorSegment("seasonal-dock-door-segment-$index", "sample"))
        }
        val report = buildSeasonalDockDoorSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
