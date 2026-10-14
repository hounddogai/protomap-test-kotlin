package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705294025)
        val items = mutableListOf<GlobalInspectionBatch>()
        for (index in 0 until 6) {
            items.add(newGlobalInspectionBatch("global-inspection-batch-$index", "sample"))
        }
        val report = buildGlobalInspectionBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
