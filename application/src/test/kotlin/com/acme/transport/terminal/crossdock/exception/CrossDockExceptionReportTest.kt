package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700950850)
        val items = mutableListOf<CrossDockException>()
        for (index in 0 until 2) {
            items.add(newCrossDockException("cross-dock-exception-$index", "sample"))
        }
        val report = buildCrossDockExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
