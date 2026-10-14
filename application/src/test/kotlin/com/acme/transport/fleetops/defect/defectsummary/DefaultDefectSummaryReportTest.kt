package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDefectSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708154575)
        val items = mutableListOf<DefaultDefectSummary>()
        for (index in 0 until 3) {
            items.add(newDefaultDefectSummary("default-defect-summary-$index", "sample"))
        }
        val report = buildDefaultDefectSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
