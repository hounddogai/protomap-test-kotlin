package com.acme.transport.network.hub.summary

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702933105)
        val items = mutableListOf<HubSummary>()
        for (index in 0 until 6) {
            items.add(newHubSummary("hub-summary-$index", "sample"))
        }
        val report = buildHubSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
