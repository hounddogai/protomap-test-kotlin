package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700867998)
        val items = mutableListOf<DwellCycle>()
        for (index in 0 until 2) {
            items.add(newDwellCycle("dwell-cycle-$index", "sample"))
        }
        val report = buildDwellCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
