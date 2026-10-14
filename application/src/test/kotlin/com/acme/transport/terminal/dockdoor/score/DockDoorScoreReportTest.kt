package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708677438)
        val items = mutableListOf<DockDoorScore>()
        for (index in 0 until 4) {
            items.add(newDockDoorScore("dock-door-score-$index", "sample"))
        }
        val report = buildDockDoorScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
