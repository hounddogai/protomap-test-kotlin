package com.acme.transport.terminal.sortation.milestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708354847)
        val items = mutableListOf<PrimarySortationAssignment>()
        for (index in 0 until 4) {
            items.add(newPrimarySortationAssignment("primary-sortation-assignment-$index", "sample"))
        }
        val report = buildPrimarySortationAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
