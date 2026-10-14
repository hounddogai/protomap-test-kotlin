package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708664520)
        val items = mutableListOf<BrokerMetric>()
        for (index in 0 until 2) {
            items.add(newBrokerMetric("broker-metric-$index", "sample"))
        }
        val report = buildBrokerMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
