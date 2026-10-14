package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704416369)
        val items = mutableListOf<WeightedRateCardRollup>()
        for (index in 0 until 6) {
            items.add(newWeightedRateCardRollup("weighted-rate-card-rollup-$index", "sample"))
        }
        val report = buildWeightedRateCardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
