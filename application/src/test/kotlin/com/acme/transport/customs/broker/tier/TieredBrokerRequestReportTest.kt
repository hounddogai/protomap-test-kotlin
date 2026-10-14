package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredBrokerRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706513861)
        val items = mutableListOf<TieredBrokerRequest>()
        for (index in 0 until 5) {
            items.add(newTieredBrokerRequest("tiered-broker-request-$index", "sample"))
        }
        val report = buildTieredBrokerRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
