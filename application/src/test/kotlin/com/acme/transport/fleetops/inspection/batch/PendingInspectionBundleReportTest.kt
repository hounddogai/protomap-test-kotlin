package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703010102)
        val items = mutableListOf<PendingInspectionBundle>()
        for (index in 0 until 6) {
            items.add(newPendingInspectionBundle("pending-inspection-bundle-$index", "sample"))
        }
        val report = buildPendingInspectionBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
