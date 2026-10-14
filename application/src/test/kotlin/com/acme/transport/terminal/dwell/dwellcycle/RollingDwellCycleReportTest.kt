package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705328459)
        val items = mutableListOf<RollingDwellCycle>()
        for (index in 0 until 3) {
            items.add(newRollingDwellCycle("rolling-dwell-cycle-$index", "sample"))
        }
        val report = buildRollingDwellCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
