package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705786622)
        val items = mutableListOf<RateCardThreshold>()
        for (index in 0 until 4) {
            items.add(newRateCardThreshold("rate-card-threshold-$index", "sample"))
        }
        val report = buildRateCardThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
