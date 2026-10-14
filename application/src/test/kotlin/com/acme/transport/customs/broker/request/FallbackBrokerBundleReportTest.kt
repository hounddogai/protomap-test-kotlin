package com.acme.transport.customs.broker.request

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBrokerBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704098088)
        val items = mutableListOf<FallbackBrokerBundle>()
        for (index in 0 until 6) {
            items.add(newFallbackBrokerBundle("fallback-broker-bundle-$index", "sample"))
        }
        val report = buildFallbackBrokerBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
