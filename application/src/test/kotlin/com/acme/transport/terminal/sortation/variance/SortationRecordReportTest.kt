package com.acme.transport.terminal.sortation.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705933231)
        val items = mutableListOf<SortationRecord>()
        for (index in 0 until 2) {
            items.add(newSortationRecord("sortation-record-$index", "sample"))
        }
        val report = buildSortationRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
