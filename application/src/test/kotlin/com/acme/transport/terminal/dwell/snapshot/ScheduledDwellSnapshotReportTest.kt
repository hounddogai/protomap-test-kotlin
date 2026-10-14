package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704289952)
        val items = mutableListOf<ScheduledDwellSnapshot>()
        for (index in 0 until 2) {
            items.add(newScheduledDwellSnapshot("scheduled-dwell-snapshot-$index", "sample"))
        }
        val report = buildScheduledDwellSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
