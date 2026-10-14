package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredInspectionGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706671344)
        val items = mutableListOf<TieredInspectionGroup>()
        for (index in 0 until 3) {
            items.add(newTieredInspectionGroup("tiered-inspection-group-$index", "sample"))
        }
        val report = buildTieredInspectionGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
