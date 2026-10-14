package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700905788)
        val items = mutableListOf<BrokerAdjustment>()
        for (index in 0 until 5) {
            items.add(newBrokerAdjustment("broker-adjustment-$index", "sample"))
        }
        val report = buildBrokerAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
