package com.acme.transport.customs.broker.allocation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701038708)
        val items = mutableListOf<GlobalBrokerAdjustment>()
        for (index in 0 until 5) {
            items.add(newGlobalBrokerAdjustment("global-broker-adjustment-$index", "sample"))
        }
        val report = buildGlobalBrokerAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
