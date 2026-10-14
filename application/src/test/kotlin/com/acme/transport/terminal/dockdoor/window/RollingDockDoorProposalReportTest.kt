package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDockDoorProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707538940)
        val items = mutableListOf<RollingDockDoorProposal>()
        for (index in 0 until 4) {
            items.add(newRollingDockDoorProposal("rolling-dock-door-proposal-$index", "sample"))
        }
        val report = buildRollingDockDoorProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
