package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701681536)
        val items = mutableListOf<TieredCrossDockBaseline>()
        for (index in 0 until 4) {
            items.add(newTieredCrossDockBaseline("tiered-cross-dock-baseline-$index", "sample"))
        }
        val report = buildTieredCrossDockBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
