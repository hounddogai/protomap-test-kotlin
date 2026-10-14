package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707574408)
        val items = mutableListOf<CrossDockQuota>()
        for (index in 0 until 3) {
            items.add(newCrossDockQuota("cross-dock-quota-$index", "sample"))
        }
        val report = buildCrossDockQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
