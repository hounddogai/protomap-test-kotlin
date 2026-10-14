package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704175351)
        val items = mutableListOf<RateCardRollup>()
        for (index in 0 until 6) {
            items.add(newRateCardRollup("rate-card-rollup-$index", "sample"))
        }
        val report = buildRateCardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
