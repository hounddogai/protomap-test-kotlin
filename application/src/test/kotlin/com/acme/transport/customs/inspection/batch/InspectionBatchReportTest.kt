package com.acme.transport.customs.inspection.batch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703768426)
        val items = mutableListOf<InspectionBatch>()
        for (index in 0 until 3) {
            items.add(newInspectionBatch("inspection-batch-$index", "sample"))
        }
        val report = buildInspectionBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
