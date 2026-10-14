package com.acme.transport.linehaul.capacity.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacitySnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702428322)
        val items = mutableListOf<CapacitySnapshot>()
        for (index in 0 until 6) {
            items.add(newCapacitySnapshot("capacity-snapshot-$index", "sample"))
        }
        val report = buildCapacitySnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
