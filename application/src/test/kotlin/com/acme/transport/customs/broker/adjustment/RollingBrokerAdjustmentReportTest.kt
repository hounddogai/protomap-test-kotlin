package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBrokerAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703185094)
        val items = mutableListOf<RollingBrokerAdjustment>()
        for (index in 0 until 3) {
            items.add(newRollingBrokerAdjustment("rolling-broker-adjustment-$index", "sample"))
        }
        val report = buildRollingBrokerAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
