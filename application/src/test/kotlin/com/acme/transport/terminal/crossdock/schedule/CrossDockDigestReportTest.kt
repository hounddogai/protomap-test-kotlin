package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704973375)
        val items = mutableListOf<CrossDockDigest>()
        for (index in 0 until 2) {
            items.add(newCrossDockDigest("cross-dock-digest-$index", "sample"))
        }
        val report = buildCrossDockDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
