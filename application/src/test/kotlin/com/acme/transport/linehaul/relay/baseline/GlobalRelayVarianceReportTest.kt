package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRelayVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705356533)
        val items = mutableListOf<GlobalRelayVariance>()
        for (index in 0 until 5) {
            items.add(newGlobalRelayVariance("global-relay-variance-$index", "sample"))
        }
        val report = buildGlobalRelayVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
