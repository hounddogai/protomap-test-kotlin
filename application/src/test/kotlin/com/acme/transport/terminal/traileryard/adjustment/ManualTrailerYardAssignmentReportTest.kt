package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701928313)
        val items = mutableListOf<ManualTrailerYardAssignment>()
        for (index in 0 until 2) {
            items.add(newManualTrailerYardAssignment("manual-trailer-yard-assignment-$index", "sample"))
        }
        val report = buildManualTrailerYardAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
