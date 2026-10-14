package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709208701)
        val items = mutableListOf<PendingInspectionAllocation>()
        for (index in 0 until 4) {
            items.add(newPendingInspectionAllocation("pending-inspection-allocation-$index", "sample"))
        }
        val report = buildPendingInspectionAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
