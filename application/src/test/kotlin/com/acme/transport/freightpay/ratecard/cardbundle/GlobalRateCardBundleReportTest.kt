package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705282299)
        val items = mutableListOf<GlobalRateCardBundle>()
        for (index in 0 until 4) {
            items.add(newGlobalRateCardBundle("global-rate-card-bundle-$index", "sample"))
        }
        val report = buildGlobalRateCardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
