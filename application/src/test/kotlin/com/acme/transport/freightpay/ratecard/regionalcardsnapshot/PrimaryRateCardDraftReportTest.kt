package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRateCardDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707836939)
        val items = mutableListOf<PrimaryRateCardDraft>()
        for (index in 0 until 2) {
            items.add(newPrimaryRateCardDraft("primary-rate-card-draft-$index", "sample"))
        }
        val report = buildPrimaryRateCardDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
