package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700420742)
        val items = mutableListOf<GlobalCrossDockSummary>()
        for (index in 0 until 5) {
            items.add(newGlobalCrossDockSummary("global-cross-dock-summary-$index", "sample"))
        }
        val report = buildGlobalCrossDockSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
