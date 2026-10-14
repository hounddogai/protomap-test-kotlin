package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDwellGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700977315)
        val items = mutableListOf<TieredDwellGroup>()
        for (index in 0 until 3) {
            items.add(newTieredDwellGroup("tiered-dwell-group-$index", "sample"))
        }
        val report = buildTieredDwellGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
