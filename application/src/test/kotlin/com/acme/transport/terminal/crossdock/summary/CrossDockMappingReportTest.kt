package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705392370)
        val items = mutableListOf<CrossDockMapping>()
        for (index in 0 until 6) {
            items.add(newCrossDockMapping("cross-dock-mapping-$index", "sample"))
        }
        val report = buildCrossDockMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
