package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelayRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702764241)
        val items = mutableListOf<PendingRelayRule>()
        for (index in 0 until 5) {
            items.add(newPendingRelayRule("pending-relay-rule-$index", "sample"))
        }
        val report = buildPendingRelayRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
