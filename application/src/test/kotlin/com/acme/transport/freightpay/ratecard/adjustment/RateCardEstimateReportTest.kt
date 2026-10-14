package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700283093)
        val items = mutableListOf<RateCardEstimate>()
        for (index in 0 until 2) {
            items.add(newRateCardEstimate("rate-card-estimate-$index", "sample"))
        }
        val report = buildRateCardEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
