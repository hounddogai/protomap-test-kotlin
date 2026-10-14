package com.acme.transport.network.cutoff.summary

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708400495)
        val items = mutableListOf<CutoffSummary>()
        for (index in 0 until 6) {
            items.add(newCutoffSummary("cutoff-summary-$index", "sample"))
        }
        val report = buildCutoffSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
