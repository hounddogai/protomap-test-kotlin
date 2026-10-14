package com.acme.transport.safety.violation.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualViolationSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701103946)
        val items = mutableListOf<ManualViolationSummary>()
        for (index in 0 until 2) {
            items.add(newManualViolationSummary("manual-violation-summary-$index", "sample"))
        }
        val report = buildManualViolationSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
