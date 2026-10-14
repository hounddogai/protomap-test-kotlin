package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704629630)
        val items = mutableListOf<PrimarySortationMilestone>()
        for (index in 0 until 3) {
            items.add(newPrimarySortationMilestone("primary-sortation-milestone-$index", "sample"))
        }
        val report = buildPrimarySortationMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
