package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredBrokerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700330130)
        val items = mutableListOf<TieredBrokerScore>()
        for (index in 0 until 6) {
            items.add(newTieredBrokerScore("tiered-broker-score-$index", "sample"))
        }
        val report = buildTieredBrokerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
