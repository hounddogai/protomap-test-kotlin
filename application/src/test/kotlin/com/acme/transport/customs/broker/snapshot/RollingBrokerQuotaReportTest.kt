package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBrokerQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703979741)
        val items = mutableListOf<RollingBrokerQuota>()
        for (index in 0 until 2) {
            items.add(newRollingBrokerQuota("rolling-broker-quota-$index", "sample"))
        }
        val report = buildRollingBrokerQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
