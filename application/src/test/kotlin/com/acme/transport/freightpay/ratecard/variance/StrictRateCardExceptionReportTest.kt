package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704997021)
        val items = mutableListOf<StrictRateCardException>()
        for (index in 0 until 6) {
            items.add(newStrictRateCardException("strict-rate-card-exception-$index", "sample"))
        }
        val report = buildStrictRateCardExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
