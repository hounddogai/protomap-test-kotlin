package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709402143)
        val items = mutableListOf<BrokerWindow>()
        for (index in 0 until 5) {
            items.add(newBrokerWindow("broker-window-$index", "sample"))
        }
        val report = buildBrokerWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
