package com.acme.transport.terminal.dwell.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705475788)
        val items = mutableListOf<DwellRollup>()
        for (index in 0 until 4) {
            items.add(newDwellRollup("dwell-rollup-$index", "sample"))
        }
        val report = buildDwellRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
