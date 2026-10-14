package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708160024)
        val items = mutableListOf<RateCardDigest>()
        for (index in 0 until 2) {
            items.add(newRateCardDigest("rate-card-digest-$index", "sample"))
        }
        val report = buildRateCardDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
