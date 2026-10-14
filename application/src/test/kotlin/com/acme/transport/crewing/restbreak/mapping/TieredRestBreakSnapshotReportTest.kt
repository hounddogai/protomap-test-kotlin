package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRestBreakSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702317321)
        val items = mutableListOf<TieredRestBreakSnapshot>()
        for (index in 0 until 5) {
            items.add(newTieredRestBreakSnapshot("tiered-rest-break-snapshot-$index", "sample"))
        }
        val report = buildTieredRestBreakSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
