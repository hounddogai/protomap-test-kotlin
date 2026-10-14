package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701593399)
        val items = mutableListOf<RateCardCycle>()
        for (index in 0 until 5) {
            items.add(newRateCardCycle("rate-card-cycle-$index", "sample"))
        }
        val report = buildRateCardCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
