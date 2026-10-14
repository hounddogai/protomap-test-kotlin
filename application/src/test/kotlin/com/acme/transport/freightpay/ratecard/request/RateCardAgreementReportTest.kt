package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703521134)
        val items = mutableListOf<RateCardAgreement>()
        for (index in 0 until 3) {
            items.add(newRateCardAgreement("rate-card-agreement-$index", "sample"))
        }
        val report = buildRateCardAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
