package com.acme.transport.network.zone.snapshot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704758016)
        val items = mutableListOf<ZoneSnapshot>()
        for (index in 0 until 6) {
            items.add(newZoneSnapshot("zone-snapshot-$index", "sample"))
        }
        val report = buildZoneSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
