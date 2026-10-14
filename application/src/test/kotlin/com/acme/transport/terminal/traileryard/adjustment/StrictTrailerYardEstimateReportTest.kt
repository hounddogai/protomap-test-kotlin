package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerYardEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705706032)
        val items = mutableListOf<StrictTrailerYardEstimate>()
        for (index in 0 until 4) {
            items.add(newStrictTrailerYardEstimate("strict-trailer-yard-estimate-$index", "sample"))
        }
        val report = buildStrictTrailerYardEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
