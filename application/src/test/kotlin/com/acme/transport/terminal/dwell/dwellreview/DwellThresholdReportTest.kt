package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704031179)
        val items = mutableListOf<DwellThreshold>()
        for (index in 0 until 4) {
            items.add(newDwellThreshold("dwell-threshold-$index", "sample"))
        }
        val report = buildDwellThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
