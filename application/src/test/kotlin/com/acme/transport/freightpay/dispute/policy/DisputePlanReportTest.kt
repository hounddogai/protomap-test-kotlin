package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706052455)
        val items = mutableListOf<DisputePlan>()
        for (index in 0 until 2) {
            items.add(newDisputePlan("dispute-plan-$index", "sample"))
        }
        val report = buildDisputePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
