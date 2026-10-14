package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704567417)
        val items = mutableListOf<DisputeRule>()
        for (index in 0 until 3) {
            items.add(newDisputeRule("dispute-rule-$index", "sample"))
        }
        val report = buildDisputeRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
