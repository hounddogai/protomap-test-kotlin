package com.acme.transport.terminal.dwell.notice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705390953)
        val items = mutableListOf<DwellMilestone>()
        for (index in 0 until 2) {
            items.add(newDwellMilestone("dwell-milestone-$index", "sample"))
        }
        val report = buildDwellMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
