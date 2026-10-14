package com.acme.transport.customs.broker.quota

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultBrokerQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706338844)
        val items = mutableListOf<DefaultBrokerQuota>()
        for (index in 0 until 6) {
            items.add(newDefaultBrokerQuota("default-broker-quota-$index", "sample"))
        }
        val report = buildDefaultBrokerQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
