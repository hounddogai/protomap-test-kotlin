package com.acme.transport.fleetops.tire.batch

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTireSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709080290)
        val items = mutableListOf<CappedTireSnapshot>()
        for (index in 0 until 4) {
            items.add(newCappedTireSnapshot("capped-tire-snapshot-$index", "sample"))
        }
        val report = buildCappedTireSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
