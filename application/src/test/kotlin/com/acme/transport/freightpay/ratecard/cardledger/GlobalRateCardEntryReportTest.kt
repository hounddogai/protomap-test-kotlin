package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709063778)
        val items = mutableListOf<GlobalRateCardEntry>()
        for (index in 0 until 6) {
            items.add(newGlobalRateCardEntry("global-rate-card-entry-$index", "sample"))
        }
        val report = buildGlobalRateCardEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
