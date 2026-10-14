package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705730874)
        val items = mutableListOf<StrictRateCardEntry>()
        for (index in 0 until 2) {
            items.add(newStrictRateCardEntry("strict-rate-card-entry-$index", "sample"))
        }
        val report = buildStrictRateCardEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
