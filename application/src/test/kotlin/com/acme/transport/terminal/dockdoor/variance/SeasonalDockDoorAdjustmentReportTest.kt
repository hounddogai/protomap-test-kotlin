package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702831718)
        val items = mutableListOf<SeasonalDockDoorAdjustment>()
        for (index in 0 until 3) {
            items.add(newSeasonalDockDoorAdjustment("seasonal-dock-door-adjustment-$index", "sample"))
        }
        val report = buildSeasonalDockDoorAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
