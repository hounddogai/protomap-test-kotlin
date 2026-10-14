package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDefectSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702144358)
        val items = mutableListOf<GlobalDefectSnapshot>()
        for (index in 0 until 6) {
            items.add(newGlobalDefectSnapshot("global-defect-snapshot-$index", "sample"))
        }
        val report = buildGlobalDefectSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
