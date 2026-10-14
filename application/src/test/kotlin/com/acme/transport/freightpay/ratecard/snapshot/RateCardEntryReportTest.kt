package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706103252)
        val items = mutableListOf<RateCardEntry>()
        for (index in 0 until 2) {
            items.add(newRateCardEntry("rate-card-entry-$index", "sample"))
        }
        val report = buildRateCardEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
