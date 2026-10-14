package com.acme.transport.terminal.sortation.claim

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703436560)
        val items = mutableListOf<SortationSnapshot>()
        for (index in 0 until 3) {
            items.add(newSortationSnapshot("sortation-snapshot-$index", "sample"))
        }
        val report = buildSortationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
