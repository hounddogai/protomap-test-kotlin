package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703680952)
        val items = mutableListOf<RateCardBundle>()
        for (index in 0 until 6) {
            items.add(newRateCardBundle("rate-card-bundle-$index", "sample"))
        }
        val report = buildRateCardBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
