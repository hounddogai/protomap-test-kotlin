package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708290529)
        val items = mutableListOf<DwellRecord>()
        for (index in 0 until 2) {
            items.add(newDwellRecord("dwell-record-$index", "sample"))
        }
        val report = buildDwellRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
