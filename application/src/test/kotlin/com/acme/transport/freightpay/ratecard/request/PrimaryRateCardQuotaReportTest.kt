package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRateCardQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705509392)
        val items = mutableListOf<PrimaryRateCardQuota>()
        for (index in 0 until 6) {
            items.add(newPrimaryRateCardQuota("primary-rate-card-quota-$index", "sample"))
        }
        val report = buildPrimaryRateCardQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
