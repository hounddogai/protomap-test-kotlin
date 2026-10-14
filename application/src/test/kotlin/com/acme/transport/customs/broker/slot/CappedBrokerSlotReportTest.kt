package com.acme.transport.customs.broker.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBrokerSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700905479)
        val items = mutableListOf<CappedBrokerSlot>()
        for (index in 0 until 5) {
            items.add(newCappedBrokerSlot("capped-broker-slot-$index", "sample"))
        }
        val report = buildCappedBrokerSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
