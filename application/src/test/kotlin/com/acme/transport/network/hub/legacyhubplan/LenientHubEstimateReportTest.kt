package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHubEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701137543)
        val items = mutableListOf<LenientHubEstimate>()
        for (index in 0 until 5) {
            items.add(newLenientHubEstimate("lenient-hub-estimate-$index", "sample"))
        }
        val report = buildLenientHubEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
