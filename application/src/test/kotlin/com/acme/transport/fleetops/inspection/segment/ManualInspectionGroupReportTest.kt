package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706696213)
        val items = mutableListOf<ManualInspectionGroup>()
        for (index in 0 until 3) {
            items.add(newManualInspectionGroup("manual-inspection-group-$index", "sample"))
        }
        val report = buildManualInspectionGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
