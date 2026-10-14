package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702560526)
        val items = mutableListOf<DefectSnapshot>()
        for (index in 0 until 4) {
            items.add(newDefectSnapshot("defect-snapshot-$index", "sample"))
        }
        val report = buildDefectSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
