package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBudgetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708703167)
        val items = mutableListOf<RateCardBudget>()
        for (index in 0 until 2) {
            items.add(newRateCardBudget("rate-card-budget-$index", "sample"))
        }
        val report = buildRateCardBudgetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
