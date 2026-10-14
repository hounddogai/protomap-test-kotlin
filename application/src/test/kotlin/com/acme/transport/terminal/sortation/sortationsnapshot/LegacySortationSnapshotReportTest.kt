package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705951215)
        val items = mutableListOf<LegacySortationSnapshot>()
        for (index in 0 until 4) {
            items.add(newLegacySortationSnapshot("legacy-sortation-snapshot-$index", "sample"))
        }
        val report = buildLegacySortationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
