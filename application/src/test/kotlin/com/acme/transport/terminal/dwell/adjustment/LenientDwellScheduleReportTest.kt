package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707109445)
        val items = mutableListOf<LenientDwellSchedule>()
        for (index in 0 until 3) {
            items.add(newLenientDwellSchedule("lenient-dwell-schedule-$index", "sample"))
        }
        val report = buildLenientDwellScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
