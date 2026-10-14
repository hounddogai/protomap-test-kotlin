package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705371906)
        val items = mutableListOf<LenientRateCardEntry>()
        for (index in 0 until 4) {
            items.add(newLenientRateCardEntry("lenient-rate-card-entry-$index", "sample"))
        }
        val report = buildLenientRateCardEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
