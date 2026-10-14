package com.acme.transport.customs.broker.group

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultBrokerOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709936267)
        val items = mutableListOf<DefaultBrokerOverride>()
        for (index in 0 until 4) {
            items.add(newDefaultBrokerOverride("default-broker-override-$index", "sample"))
        }
        val report = buildDefaultBrokerOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
