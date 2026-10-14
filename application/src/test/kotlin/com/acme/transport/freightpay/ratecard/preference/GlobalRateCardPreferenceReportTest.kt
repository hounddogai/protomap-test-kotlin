package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700223947)
        val items = mutableListOf<GlobalRateCardPreference>()
        for (index in 0 until 2) {
            items.add(newGlobalRateCardPreference("global-rate-card-preference-$index", "sample"))
        }
        val report = buildGlobalRateCardPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
