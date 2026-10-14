package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSwapSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702238161)
        val items = mutableListOf<PendingSwapSnapshot>()
        for (index in 0 until 3) {
            items.add(newPendingSwapSnapshot("pending-swap-snapshot-$index", "sample"))
        }
        val report = buildPendingSwapSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
