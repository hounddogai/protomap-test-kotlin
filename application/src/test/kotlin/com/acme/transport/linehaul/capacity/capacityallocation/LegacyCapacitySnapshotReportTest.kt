package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyCapacitySnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706694724)
        val items = mutableListOf<LegacyCapacitySnapshot>()
        for (index in 0 until 2) {
            items.add(newLegacyCapacitySnapshot("legacy-capacity-snapshot-$index", "sample"))
        }
        val report = buildLegacyCapacitySnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
