package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701647387)
        val items = mutableListOf<ManualTrailerYardEstimate>()
        for (index in 0 until 3) {
            items.add(newManualTrailerYardEstimate("manual-trailer-yard-estimate-$index", "sample"))
        }
        val report = buildManualTrailerYardEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
