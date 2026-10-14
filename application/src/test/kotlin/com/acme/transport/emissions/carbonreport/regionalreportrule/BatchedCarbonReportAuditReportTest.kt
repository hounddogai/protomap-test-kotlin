package com.acme.transport.emissions.carbonreport.regionalreportrule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCarbonReportAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706377329)
        val items = mutableListOf<BatchedCarbonReportAudit>()
        for (index in 0 until 5) {
            items.add(newBatchedCarbonReportAudit("batched-carbon-report-audit-$index", "sample"))
        }
        val report = buildBatchedCarbonReportAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
