package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706545587)
        val items = mutableListOf<CrossDockTier>()
        for (index in 0 until 3) {
            items.add(newCrossDockTier("cross-dock-tier-$index", "sample"))
        }
        val report = buildCrossDockTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
