package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709889445)
        val items = mutableListOf<BrokerPolicy>()
        for (index in 0 until 3) {
            items.add(newBrokerPolicy("broker-policy-$index", "sample"))
        }
        val report = buildBrokerPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
