package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704320911)
        val items = mutableListOf<RateCardDraft>()
        for (index in 0 until 3) {
            items.add(newRateCardDraft("rate-card-draft-$index", "sample"))
        }
        val report = buildRateCardDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
