package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDockDoorSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702594251)
        val items = mutableListOf<SeasonalDockDoorSnapshot>()
        for (index in 0 until 6) {
            items.add(newSeasonalDockDoorSnapshot("seasonal-dock-door-snapshot-$index", "sample"))
        }
        val report = buildSeasonalDockDoorSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
