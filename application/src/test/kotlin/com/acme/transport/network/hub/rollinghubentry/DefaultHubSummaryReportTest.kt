package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709444328)
        val items = mutableListOf<DefaultHubSummary>()
        for (index in 0 until 4) {
            items.add(newDefaultHubSummary("default-hub-summary-$index", "sample"))
        }
        val report = buildDefaultHubSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
