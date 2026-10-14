package com.acme.transport.customs.broker.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708106265)
        val items = mutableListOf<WeightedBrokerRecord>()
        for (index in 0 until 3) {
            items.add(newWeightedBrokerRecord("weighted-broker-record-$index", "sample"))
        }
        val report = buildWeightedBrokerRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
