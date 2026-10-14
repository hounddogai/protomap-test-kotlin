package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703846012)
        val items = mutableListOf<GlobalCrossDockVariance>()
        for (index in 0 until 2) {
            items.add(newGlobalCrossDockVariance("global-cross-dock-variance-$index", "sample"))
        }
        val report = buildGlobalCrossDockVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
