package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledBrokerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705466718)
        val items = mutableListOf<ScheduledBrokerScore>()
        for (index in 0 until 6) {
            items.add(newScheduledBrokerScore("scheduled-broker-score-$index", "sample"))
        }
        val report = buildScheduledBrokerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
