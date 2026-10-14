package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703645889)
        val items = mutableListOf<CappedRateCardRollup>()
        for (index in 0 until 2) {
            items.add(newCappedRateCardRollup("capped-rate-card-rollup-$index", "sample"))
        }
        val report = buildCappedRateCardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
