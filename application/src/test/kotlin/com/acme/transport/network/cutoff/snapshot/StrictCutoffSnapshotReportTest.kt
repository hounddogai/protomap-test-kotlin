package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCutoffSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704390624)
        val items = mutableListOf<StrictCutoffSnapshot>()
        for (index in 0 until 5) {
            items.add(newStrictCutoffSnapshot("strict-cutoff-snapshot-$index", "sample"))
        }
        val report = buildStrictCutoffSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
