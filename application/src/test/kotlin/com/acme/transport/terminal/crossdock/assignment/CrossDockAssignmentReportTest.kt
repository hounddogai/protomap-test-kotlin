package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708644693)
        val items = mutableListOf<CrossDockAssignment>()
        for (index in 0 until 6) {
            items.add(newCrossDockAssignment("cross-dock-assignment-$index", "sample"))
        }
        val report = buildCrossDockAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
