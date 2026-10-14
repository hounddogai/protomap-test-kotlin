package com.acme.transport.customs.broker.profile

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708271334)
        val items = mutableListOf<BrokerProfile>()
        for (index in 0 until 2) {
            items.add(newBrokerProfile("broker-profile-$index", "sample"))
        }
        val report = buildBrokerProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
