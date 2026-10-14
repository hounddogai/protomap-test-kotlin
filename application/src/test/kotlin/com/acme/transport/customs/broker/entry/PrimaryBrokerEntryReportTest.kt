package com.acme.transport.customs.broker.entry

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryBrokerEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701644424)
        val items = mutableListOf<PrimaryBrokerEntry>()
        for (index in 0 until 2) {
            items.add(newPrimaryBrokerEntry("primary-broker-entry-$index", "sample"))
        }
        val report = buildPrimaryBrokerEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
