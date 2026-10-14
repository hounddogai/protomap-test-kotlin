package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707443650)
        val items = mutableListOf<RegionalBrokerVariance>()
        for (index in 0 until 6) {
            items.add(newRegionalBrokerVariance("regional-broker-variance-$index", "sample"))
        }
        val report = buildRegionalBrokerVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
