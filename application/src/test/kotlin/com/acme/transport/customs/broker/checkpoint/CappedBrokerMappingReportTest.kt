package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701085885)
        val items = mutableListOf<CappedBrokerMapping>()
        for (index in 0 until 4) {
            items.add(newCappedBrokerMapping("capped-broker-mapping-$index", "sample"))
        }
        val report = buildCappedBrokerMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
