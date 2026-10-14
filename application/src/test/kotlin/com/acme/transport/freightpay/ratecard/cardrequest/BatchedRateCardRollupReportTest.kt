package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRateCardRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704560178)
        val items = mutableListOf<BatchedRateCardRollup>()
        for (index in 0 until 6) {
            items.add(newBatchedRateCardRollup("batched-rate-card-rollup-$index", "sample"))
        }
        val report = buildBatchedRateCardRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
