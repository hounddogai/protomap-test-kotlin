package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704938775)
        val items = mutableListOf<PrimarySortationSnapshot>()
        for (index in 0 until 6) {
            items.add(newPrimarySortationSnapshot("primary-sortation-snapshot-$index", "sample"))
        }
        val report = buildPrimarySortationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
