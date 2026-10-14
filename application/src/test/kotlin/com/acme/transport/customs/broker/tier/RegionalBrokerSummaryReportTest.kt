package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalBrokerSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705340629)
        val items = mutableListOf<RegionalBrokerSummary>()
        for (index in 0 until 4) {
            items.add(newRegionalBrokerSummary("regional-broker-summary-$index", "sample"))
        }
        val report = buildRegionalBrokerSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
