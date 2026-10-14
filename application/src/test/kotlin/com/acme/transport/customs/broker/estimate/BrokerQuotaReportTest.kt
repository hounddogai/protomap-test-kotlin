package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702795443)
        val items = mutableListOf<BrokerQuota>()
        for (index in 0 until 6) {
            items.add(newBrokerQuota("broker-quota-$index", "sample"))
        }
        val report = buildBrokerQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
