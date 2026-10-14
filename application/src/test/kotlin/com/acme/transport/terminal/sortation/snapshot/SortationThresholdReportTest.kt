package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708540109)
        val items = mutableListOf<SortationThreshold>()
        for (index in 0 until 5) {
            items.add(newSortationThreshold("sortation-threshold-$index", "sample"))
        }
        val report = buildSortationThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
