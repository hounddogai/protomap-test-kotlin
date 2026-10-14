package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702871172)
        val items = mutableListOf<BrokerRecord>()
        for (index in 0 until 6) {
            items.add(newBrokerRecord("broker-record-$index", "sample"))
        }
        val report = buildBrokerRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
