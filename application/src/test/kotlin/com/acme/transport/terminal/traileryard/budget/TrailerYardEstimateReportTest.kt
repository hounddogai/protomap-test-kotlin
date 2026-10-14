package com.acme.transport.terminal.traileryard.budget

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702036550)
        val items = mutableListOf<TrailerYardEstimate>()
        for (index in 0 until 5) {
            items.add(newTrailerYardEstimate("trailer-yard-estimate-$index", "sample"))
        }
        val report = buildTrailerYardEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
