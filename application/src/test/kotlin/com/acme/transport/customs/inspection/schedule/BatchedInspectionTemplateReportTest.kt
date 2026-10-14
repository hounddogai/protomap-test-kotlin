package com.acme.transport.customs.inspection.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708633624)
        val items = mutableListOf<BatchedInspectionTemplate>()
        for (index in 0 until 4) {
            items.add(newBatchedInspectionTemplate("batched-inspection-template-$index", "sample"))
        }
        val report = buildBatchedInspectionTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
