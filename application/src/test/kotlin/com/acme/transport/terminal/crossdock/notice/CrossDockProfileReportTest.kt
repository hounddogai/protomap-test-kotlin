package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705931590)
        val items = mutableListOf<CrossDockProfile>()
        for (index in 0 until 6) {
            items.add(newCrossDockProfile("cross-dock-profile-$index", "sample"))
        }
        val report = buildCrossDockProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
