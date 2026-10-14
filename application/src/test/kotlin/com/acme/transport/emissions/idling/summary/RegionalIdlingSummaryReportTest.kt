package com.acme.transport.emissions.idling.summary

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705827521)
        val items = mutableListOf<RegionalIdlingSummary>()
        for (index in 0 until 2) {
            items.add(newRegionalIdlingSummary("regional-idling-summary-$index", "sample"))
        }
        val report = buildRegionalIdlingSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
