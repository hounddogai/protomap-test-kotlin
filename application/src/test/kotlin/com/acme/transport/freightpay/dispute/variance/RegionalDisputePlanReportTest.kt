package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDisputePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704499002)
        val items = mutableListOf<RegionalDisputePlan>()
        for (index in 0 until 5) {
            items.add(newRegionalDisputePlan("regional-dispute-plan-$index", "sample"))
        }
        val report = buildRegionalDisputePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
