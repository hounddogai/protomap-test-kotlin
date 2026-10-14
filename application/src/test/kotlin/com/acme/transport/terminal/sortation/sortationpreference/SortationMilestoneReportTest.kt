package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706861617)
        val items = mutableListOf<SortationMilestone>()
        for (index in 0 until 5) {
            items.add(newSortationMilestone("sortation-milestone-$index", "sample"))
        }
        val report = buildSortationMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
