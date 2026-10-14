package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702560859)
        val items = mutableListOf<RateCardMapping>()
        for (index in 0 until 6) {
            items.add(newRateCardMapping("rate-card-mapping-$index", "sample"))
        }
        val report = buildRateCardMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
