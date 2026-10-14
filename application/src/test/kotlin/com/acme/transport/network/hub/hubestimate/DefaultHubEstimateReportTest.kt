package com.acme.transport.network.hub.hubestimate

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700290298)
        val items = mutableListOf<DefaultHubEstimate>()
        for (index in 0 until 3) {
            items.add(newDefaultHubEstimate("default-hub-estimate-$index", "sample"))
        }
        val report = buildDefaultHubEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
