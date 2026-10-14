package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700222332)
        val items = mutableListOf<TrailerYardCycle>()
        for (index in 0 until 4) {
            items.add(newTrailerYardCycle("trailer-yard-cycle-$index", "sample"))
        }
        val report = buildTrailerYardCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
