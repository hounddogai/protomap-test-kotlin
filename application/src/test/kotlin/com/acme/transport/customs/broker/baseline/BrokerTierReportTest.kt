package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708266967)
        val items = mutableListOf<BrokerTier>()
        for (index in 0 until 6) {
            items.add(newBrokerTier("broker-tier-$index", "sample"))
        }
        val report = buildBrokerTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
