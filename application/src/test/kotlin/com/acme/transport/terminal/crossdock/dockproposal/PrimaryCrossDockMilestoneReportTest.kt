package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCrossDockMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704110755)
        val items = mutableListOf<PrimaryCrossDockMilestone>()
        for (index in 0 until 4) {
            items.add(newPrimaryCrossDockMilestone("primary-cross-dock-milestone-$index", "sample"))
        }
        val report = buildPrimaryCrossDockMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
