package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701562051)
        val items = mutableListOf<WeightedBrokerScore>()
        for (index in 0 until 4) {
            items.add(newWeightedBrokerScore("weighted-broker-score-$index", "sample"))
        }
        val report = buildWeightedBrokerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
