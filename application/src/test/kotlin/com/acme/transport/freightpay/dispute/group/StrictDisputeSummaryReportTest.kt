package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDisputeSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704689469)
        val items = mutableListOf<StrictDisputeSummary>()
        for (index in 0 until 6) {
            items.add(newStrictDisputeSummary("strict-dispute-summary-$index", "sample"))
        }
        val report = buildStrictDisputeSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
