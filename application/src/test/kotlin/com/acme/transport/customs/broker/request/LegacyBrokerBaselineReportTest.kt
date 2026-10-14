package com.acme.transport.customs.broker.request

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBrokerBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701614236)
        val items = mutableListOf<LegacyBrokerBaseline>()
        for (index in 0 until 2) {
            items.add(newLegacyBrokerBaseline("legacy-broker-baseline-$index", "sample"))
        }
        val report = buildLegacyBrokerBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
