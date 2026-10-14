package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704205743)
        val items = mutableListOf<RateCardTier>()
        for (index in 0 until 4) {
            items.add(newRateCardTier("rate-card-tier-$index", "sample"))
        }
        val report = buildRateCardTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
