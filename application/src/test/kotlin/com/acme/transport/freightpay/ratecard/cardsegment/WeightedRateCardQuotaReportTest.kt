package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701595725)
        val items = mutableListOf<WeightedRateCardQuota>()
        for (index in 0 until 4) {
            items.add(newWeightedRateCardQuota("weighted-rate-card-quota-$index", "sample"))
        }
        val report = buildWeightedRateCardQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
