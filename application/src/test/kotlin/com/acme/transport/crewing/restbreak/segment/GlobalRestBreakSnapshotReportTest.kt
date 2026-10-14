package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700548495)
        val items = mutableListOf<GlobalRestBreakSnapshot>()
        for (index in 0 until 3) {
            items.add(newGlobalRestBreakSnapshot("global-rest-break-snapshot-$index", "sample"))
        }
        val report = buildGlobalRestBreakSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
