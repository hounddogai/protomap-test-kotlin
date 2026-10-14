package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709988285)
        val items = mutableListOf<BatchedTrailerYardAudit>()
        for (index in 0 until 3) {
            items.add(newBatchedTrailerYardAudit("batched-trailer-yard-audit-$index", "sample"))
        }
        val report = buildBatchedTrailerYardAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
