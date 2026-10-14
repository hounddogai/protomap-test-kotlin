package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709105616)
        val items = mutableListOf<RateCardAllocation>()
        for (index in 0 until 4) {
            items.add(newRateCardAllocation("rate-card-allocation-$index", "sample"))
        }
        val report = buildRateCardAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
